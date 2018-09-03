appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%date{yyyy-MM-dd'T'HH:mm:ss.SSSZ} %level [%thread] %logger - %msg%n"
    }
}

root(DEBUG, ["CONSOLE"])