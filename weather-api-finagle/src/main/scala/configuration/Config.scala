package configuration

import com.typesafe.config.ConfigFactory

trait Config {
  def config = LoadedConfig.config
}

object LoadedConfig {
  lazy val config = ConfigFactory.load
}