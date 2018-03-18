package com.hwg.generate

import slogging._

object Generate extends App with LazyLogging {
  LoggerConfig.factory = PrintLoggerFactory()

  LoggerConfig.level = LogLevel.DEBUG

  logger.info("Beginning Resource Generation")
  logger.info("\n\n===========================\n")

  GenSystem.generate(31687)

  logger.info("\n\n===========================\n")
  logger.info("Generation Complete")
}
