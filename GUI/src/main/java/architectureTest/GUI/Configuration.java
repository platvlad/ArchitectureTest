package architectureTest.GUI;


import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private Map<String, ExperimentConfiguration> paramExperiments = new HashMap<>();

    private String architectureType;

    public void setArchitectureType(String architecture) {
        architectureType = architecture;
    }

    public void addParamExperiment(String param) {
        paramExperiments.put(param, new ExperimentConfiguration());
    }

    public void addParameter(String floatingParam, String fixedParam) {
        paramExperiments.get(floatingParam).addParameter(fixedParam);
    }

    public void setMinParameter(String floatingParam, String editedParam, int value) {
        paramExperiments.get(floatingParam).setMinParameter(editedParam, value);
    }

    public void setMaxParameter(String floatingParam, String editedParam, int value) {
        paramExperiments.get(floatingParam).setMaxParameter(editedParam, value);
    }

    public void setStepParameter(String floatingParam, String editedParam, String value) {
        paramExperiments.get(floatingParam).setStepParameter(editedParam, value);
    }

    public void setFixedParameter(String floatingParam, String editedParam, String value) {
        paramExperiments.get(floatingParam).setFixedParameter(editedParam, value);
    }

    public Configuration() {
    }

    public static class ExperimentConfiguration {

        public static class Parameter {
            private String name;
            private int fixedValue;
            private int minValue;
            private int maxValue;
            private int step;

            @Override
            public String toString() {
                return "name: " + name + "; fixedValue = " + fixedValue + "; minValue = " + minValue + "; maxValue = " + maxValue + "; step = " + step;
            }
        }

        Map<String, Parameter> parameters = new HashMap<>();

        public void setMinParameter(String paramName, int value) {
            parameters.get(paramName).minValue = value;
            //System.out.println(parameters.get(paramName));
        }

        public void setMaxParameter(String paramName, int value) {
            parameters.get(paramName).maxValue = value;
            //System.out.println(parameters.get(paramName));
        }

        public void setStepParameter(String paramName, String value) {

            parameters.get(paramName).step = Integer.parseInt(value);
            //System.out.println(parameters.get(paramName));
        }

        public void setFixedParameter(String paramName, String value) {
            parameters.get(paramName).fixedValue = Integer.parseInt(value);
            //System.out.println(parameters.get(paramName));
        }

        public void addParameter(String name) {
            parameters.put(name, new Parameter());
        }

    }
}
