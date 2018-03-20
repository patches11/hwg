package com.hwg.generate

import com.hwg.universe.Universe
import slogging._

object Generate extends App with LazyLogging {
  LoggerConfig.factory = PrintLoggerFactory()

  LoggerConfig.level = LogLevel.DEBUG

  logger.info("Beginning Resource Generation")
  logger.info("\n\n===========================\n")

  Universe.systems.foreach { system =>
    GenSystem.generate(SystemOptions(system))
  }

  logger.info("\n\n===========================\n")
  logger.info("Generation Complete")
}
