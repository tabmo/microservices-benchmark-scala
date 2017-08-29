package dataprovider

import com.twitter.finagle
import com.twitter.finagle.http.{Response, Request}

trait Service extends finagle.Service[Request, Response]
