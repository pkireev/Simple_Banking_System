class Converter {

    /**
     * It returns a double value or 0 if an exception occurred
     */
    public static double convertStringToDouble(String input) {

        double result = 0;
        try {
            result = Double.parseDouble(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}