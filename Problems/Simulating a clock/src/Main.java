class Clock {

    int hours = 12;
    int minutes = 0;

    void next() {
        minutes++;
        if (minutes == 60) {
            hours++;
            minutes = 0;
        }
        if (hours == 13) {
            hours = 1;
        }
    }
}