package com.codigeria.loguerro;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class LoguerroCliRunner
{
    private final Logger logger;

    private final List<String> arguments;

    LoguerroCliRunner(List<String> arguments)
    {
        this(arguments, LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()));
    }

    @VisibleForTesting
    LoguerroCliRunner(List<String> arguments, Logger logger)
    {
        this.arguments = Collections.unmodifiableList(requireNonNull(arguments));
        this.logger = logger;
    }

    void run()
    {
        logger.debug("Running CLI runner...");
    }
}
