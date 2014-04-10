/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime;

import org.apache.log4j.Logger;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.HttpProcessor;
import org.webharvest.runtime.processors.ProcessorResolver;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Stack;

import java.util.*;

/**
 * Basic runtime class.
 */
public class Scraper {

    public static final int STATUS_READY = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_STOPPED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_EXIT = 6;

    private Logger logger = Logger.getLogger("" + System.currentTimeMillis());
    private ScraperConfiguration configuration;
    private String workingDir;
    private ScraperContext context;

    private RuntimeConfig runtimeConfig;

    private transient boolean isDebugMode = false;

    private HttpClientManager httpClientManager;

    // stack of running functions
    private transient Stack runningFunctions = new Stack();

    // params that are proceeded to calling function
    private transient Map functionParams = new HashMap();

    // stack of running http processors
    private transient Stack runningHttpProcessors = new Stack();

    // shows depth of running processors during execution 
    private transient int runningLevel = 1;

    // default script engine used throughout the configuration execution
    private ScriptEngine scriptEngine = null;

    // all used script engines in this scraper
    private Map usedScriptEngines = new HashMap();

    //currently running processor
    private BaseProcessor runningProcessor;

    private List scraperRuntimeListeners = new LinkedList();

    private int status = STATUS_READY;

    private String message = null;

    /**
     * Constructor.
     * @param configuration
     * @param workingDir
     */
    public Scraper(ScraperConfiguration configuration, String workingDir) {
        this.configuration = configuration;
        this.runtimeConfig = new RuntimeConfig();
        this.workingDir = CommonUtil.adaptFilename(workingDir);

        this.httpClientManager = new HttpClientManager();

        this.context = new ScraperContext(this);
        this.scriptEngine = configuration.createScriptEngine(this.context);
        this.usedScriptEngines.put(configuration.getDefaultScriptEngine(), this.scriptEngine);
    }

    /**
     * Adds parameter with specified name and value to the context.
     * This way some predefined variables can be put in runtime context
     * before execution starts.
     * @param name
     * @param value
     */
    public void addVariableToContext(String name, Object value) {
        this.context.put(name, new NodeVariable(value));
    }

    /**
     * Add all map values to the context.
     * @param map
     */
    public void addVariablesToContext(Map map) {
        if (map != null) {
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                this.context.put( entry.getKey(), new NodeVariable(entry.getValue()) );
            }
        }
    }

    public Variable execute(List ops) {
        this.setStatus(STATUS_RUNNING);

        // inform al listeners that execution is just about to start
        Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
        while (listenersIterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
            listener.onExecutionStart(this);
        }

        Iterator it = ops.iterator();
        while (it.hasNext()) {
            IElementDef elementDef = (IElementDef) it.next();
            BaseProcessor processor = ProcessorResolver.createProcessor(elementDef, this.configuration, this);

            if (processor != null) {
                processor.run(this, context);
            }
        }

        return new NodeVariable("");
    }

    public void execute() {
    	long startTime = System.currentTimeMillis();

        execute( configuration.getOperations() );

        if ( this.status == STATUS_RUNNING ) {
            this.setStatus(STATUS_FINISHED);
        }

        // inform al listeners that execution is finished
        Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
        while (listenersIterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
            listener.onExecutionEnd(this);
        }

        if ( logger.isInfoEnabled() ) {
            if (this.status == STATUS_FINISHED) {
                logger.info("Configuration executed in " + (System.currentTimeMillis() - startTime) + "ms.");
            } else if (this.status == STATUS_STOPPED) {
                logger.info("Configuration stopped!");
            }
        }
    }
    
    public ScraperContext getContext() {
		return context;
	}

	public ScraperConfiguration getConfiguration() {
        return configuration;
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public void addRunningFunction(CallProcessor callProcessor) {
        runningFunctions.push(callProcessor);
    }

    public CallProcessor getRunningFunction() {
        return (CallProcessor) runningFunctions.peek();
    }

    public void clearFunctionParams() {
        this.functionParams.clear();
    }

    public void addFunctionParam(String name, Variable value) {
        this.functionParams.put(name, value);
    }

    public Map getFunctionParams() {
        return functionParams;
    }

    public void removeRunningFunction() {
        if (runningFunctions.size() > 0) {
            runningFunctions.pop();
        }
    }
    
    public HttpProcessor getRunningHttpProcessor() {
    	return (HttpProcessor) runningHttpProcessors.peek();
    }
    
    public void setRunningHttpProcessor(HttpProcessor httpProcessor) {
    	runningHttpProcessors.push(httpProcessor);
    }

    public void removeRunningHttpProcessor() {
        if (runningHttpProcessors.size() > 0) {
            runningHttpProcessors.pop();
        }
    }
    public void increaseRunningLevel() {
        this.runningLevel++;
    }

    public void decreaseRunningLevel() {
        this.runningLevel--;
    }

    public int getRunningLevel() {
        return runningLevel;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebug(boolean debug) {
        this.isDebugMode = debug;
    }

    public ScriptEngine getScriptEngine() {
        return runningFunctions.size() > 0 ? getRunningFunction().getScriptEngine() : this.scriptEngine;
    }

    public synchronized ScriptEngine getScriptEngine(String engineType) {
        ScriptEngine engine = (ScriptEngine) this.usedScriptEngines.get(engineType);
        if (engine == null) {
            engine = configuration.createScriptEngine(this.context, engineType);
            this.usedScriptEngines.put(engineType, engine);
        }

        return engine;
    }

    public Logger getLogger() {
        return logger;
    }

    public BaseProcessor getRunningProcessor() {
        return runningProcessor;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    public void setExecutingProcessor(BaseProcessor processor) {
        this.runningProcessor = processor;
        Iterator iterator = this.scraperRuntimeListeners.iterator();
        while (iterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) iterator.next();
            listener.onNewProcessorExecution(this, processor);
        }
    }

    public void processorFinishedExecution(BaseProcessor processor, Map properties) {
        Iterator iterator = this.scraperRuntimeListeners.iterator();
        while (iterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) iterator.next();
            listener.onProcessorExecutionFinished(this, processor, properties);
        }
    }

    public void addRuntimeListener(ScraperRuntimeListener listener) {
        this.scraperRuntimeListeners.add(listener);
    }

    public void removeRuntimeListener(ScraperRuntimeListener listener) {
        this.scraperRuntimeListeners.remove(listener);
    }

    public synchronized int getStatus() {
        return status;
    }

    private synchronized void setStatus(int status) {
        this.status = status;
    }

    public void stopExecution() {
        setStatus(STATUS_STOPPED);
    }

    public void exitExecution(String message) {
        setStatus(STATUS_EXIT);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void pauseExecution() {
        if (this.status == STATUS_RUNNING) {
            setStatus(STATUS_PAUSED);

            // inform al listeners that execution is paused
            Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
            while (listenersIterator.hasNext()) {
                ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
                listener.onExecutionPaused(this);
            }
        }
    }

    public void continueExecution() {
        if (this.status == STATUS_PAUSED) {
            setStatus(STATUS_RUNNING);

            // inform al listeners that execution is continued
            Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
            while (listenersIterator.hasNext()) {
                ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
                listener.onExecutionContinued(this);
            }
        }
    }

    /**
     * Inform all scraper listeners that an error has occured during scraper execution.
     */
    public void informListenersAboutError(Exception e) {
        setStatus(STATUS_ERROR);

        // inform al listeners that execution is continued
        Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
        while (listenersIterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
            listener.onExecutionError(this, e);
        }
    }

    public void dispose() {
        // empty scraper's variable context
        this.context.clear();

        // free connection with context
        this.context.dispose();

        // releases script engines
        if (this.usedScriptEngines != null) {
            Iterator iterator = this.usedScriptEngines.values().iterator();
            while (iterator.hasNext()) {
                ScriptEngine engine = (ScriptEngine) iterator.next();
                if (engine != null) {
                    engine.dispose();
                }
            }
        }

        this.logger.removeAllAppenders();
        
        Iterator iterator = usedScriptEngines.values().iterator();
        while (iterator.hasNext()) {
            ScriptEngine engine = (ScriptEngine) iterator.next();
            engine.dispose();
        }
    }

}