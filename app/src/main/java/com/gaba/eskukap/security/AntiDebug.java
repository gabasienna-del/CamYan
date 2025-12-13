package com.gaba.eskukap.security;

public final class AntiDebug {

    public static final AntiDebug INSTANCE = new AntiDebug();

    private AntiDebug() {}

    public Result scan() {
        return new Result(
                false, // debugger
                false, // tracer
                false, // tracerPid
                false, // frida tcp
                false, // frida tcp6
                false, // frida unix
                false, // maps
                false  // xposed/frida class
        );
    }

    public void startWatchdog(long periodMs) {
        // stub — логика может быть добавлена позже
    }

    public static final class Result {

        private final boolean suspicious;

        public Result(
                boolean a,
                boolean b,
                boolean c,
                boolean d,
                boolean e,
                boolean f,
                boolean g,
                boolean h
        ) {
            this.suspicious = a || b || c || d || e || f || g || h;
        }

        public boolean getSuspicious() {
            return suspicious;
        }

        @Override
        public String toString() {
            return "AntiDebug.Result{suspicious=" + suspicious + "}";
        }
    }
}
