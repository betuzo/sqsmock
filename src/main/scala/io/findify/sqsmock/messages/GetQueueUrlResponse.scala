package io.findify.sqsmock.messages

import io.findify.sqsmock.model.Queue

/**
  * Created by shutty on 3/29/16.
  */
case class GetQueueUrlResponse(queue:Queue) extends Response {
  def toXML =
    <GetQueueUrlResponse>
      <GetQueueUrlResult>
        <QueueUrl>{queue.url}</QueueUrl>
      </GetQueueUrlResult>
      <ResponseMetadata>
        <RequestId>{uuid}</RequestId>
      </ResponseMetadata>
    </GetQueueUrlResponse>
}
