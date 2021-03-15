class Problem {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("test".equals(args[i])) {
                System.out.println(i);
                return;
            }
        }
        System.out.println(-1);
    }
}