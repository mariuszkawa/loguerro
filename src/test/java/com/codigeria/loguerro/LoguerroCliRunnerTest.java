package com.codigeria.loguerro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LoguerroCliRunnerTest
{
    @Mock
    List<String> arguments;
    @Mock
    Logger logger;

    @InjectMocks
    LoguerroCliRunner loguerroCliRunner;

    @Test
    void run___given_empty_arg_list___when_run_CLI_runner___then_log_the_event_at_debug_level()
    {
        // Given
        // (empty list of arguments)

        // When
        loguerroCliRunner.run();

        // Then
        verify(logger, times(1)).debug(eq("Running CLI runner..."));
    }
}