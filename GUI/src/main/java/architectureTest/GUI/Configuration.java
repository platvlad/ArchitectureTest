package architectureTest.GUI;


import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private Map<String, ExperimentConfiguration> paramExperiments = new HashMap<>();

    private String architectureType;

    private String floatingParam;

    public String getArchitectureType() {
        return architectureType;
    }

    public String getFloatingParam() {
        return floatingParam;
    }

    public ExperimentConfiguration getCurrentExperiment() {
        return paramExperiments.get(floatingParam).copy();
    }

    public synchronized void setArchitectureType(String architecture) {
        architectureType = architecture;
    }

    public synchronized void setFloatingParam(String param) {
        floatingParam = param;
    }

    public synchronized void addParamExperiment(String param) {
        paramExperiments.put(param, new ExperimentConfiguration());
    }

    public synchronized void addParameter(String floatingParam, String fixedParam) {
        paramExperiments.get(floatingParam).addParameter(fixedParam);
    }

    public synchronized void setMinParameter(String floatingParam, String editedParam, int value) {
        paramExperiments.get(floatingParam).setMinParameter(editedParam, value);
    }

    public synchronized void setMaxParameter(String floatingParam, String editedParam, int value) {
        paramExperiments.get(floatingParam).setMaxParameter(editedParam, value);
    }

    public synchronized void setStepParameter(String floatingParam, String editedParam, String value) {
        paramExperiments.get(floatingParam).setStepParameter(editedParam, value);
    }

    public synchronized void setFixedParameter(String floatingParam, String editedParam, String value) {
        paramExperiments.get(floatingParam).setFixedParameter(editedParam, value);
    }

    public Configuration() {
    }

}
