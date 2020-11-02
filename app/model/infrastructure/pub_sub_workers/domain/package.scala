package model.infrastructure.pub_sub_workers

package object domain {
  type Serialized = String
  def segregateTopicById: String => (String => String) = topicName => (id => s"$topicName-$id")
}
