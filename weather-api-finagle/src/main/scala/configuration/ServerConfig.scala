package configuration

trait ServerConfig extends Config {
  val SERVER_PORT = config.getInt("application.server.listen.port")
  val MAX_CONCURRENT_REQUEST = config.getInt("application.server.maxConcurrentRequest")
  val CLIENT_MAX_CONCURRENT_REQUEST = config.getInt("application.clientMaxConcurrentRequest")
}
