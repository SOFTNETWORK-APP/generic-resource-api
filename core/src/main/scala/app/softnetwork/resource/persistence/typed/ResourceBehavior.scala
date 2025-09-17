package app.softnetwork.resource.persistence.typed

import akka.actor.typed.scaladsl.{ActorContext, TimerScheduler}
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import app.softnetwork.resource.message.ResourceEvents._
import app.softnetwork.resource.message.ResourceMessages._
import app.softnetwork.resource.model.Resource

import scala.language.implicitConversions
import app.softnetwork.persistence.ManifestWrapper
import app.softnetwork.persistence.typed._
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.utils.{Base64Tools, HashTools}
import org.apache.tika.Tika

import java.io.ByteArrayInputStream
import java.time.Instant
import scala.util.{Failure, Success, Try}

/** Created by smanciot on 30/04/2020.
  */
sealed trait ResourceBehavior
    extends TimeStampedBehavior[ResourceCommand, Resource, ResourceEvent, ResourceResult]
    with ManifestWrapper[Resource] {

  override protected val manifestWrapper: ManifestW = ManifestW()

  /** @return
    *   node role required to start this actor
    */
  override def role: String = ResourceSettings.AkkaNodeRole

  /** Set event tags, which will be used in persistence query
    *
    * @param entityId
    *   - entity id
    * @param event
    *   - the event to tag
    * @return
    *   event tags
    */
  override protected def tagEvent(entityId: String, event: ResourceEvent): Set[String] = {
    event match {
      case _: SessionResourceEvent =>
        Set(
          s"${persistenceId.toLowerCase}-to-session"
        )
      case _ =>
        Set(
          persistenceId,
          s"${persistenceId.toLowerCase}-to-localfilesystem",
          s"${persistenceId.toLowerCase}-to-s3",
          s"${persistenceId.toLowerCase}-to-gcs",
          s"${persistenceId.toLowerCase}-to-azure",
          s"${persistenceId.toLowerCase}-to-minio",
          s"${persistenceId.toLowerCase}-to-db", // database storage (Cassandra, Postgres, MySQL...)
          s"${persistenceId.toLowerCase}-to-redis" // redis storage
        )
    }
  }

  /** @param entityId
    *   - entity identity
    * @param state
    *   - current state
    * @param command
    *   - command to handle
    * @param replyTo
    *   - optional actor to reply to
    * @return
    *   effect
    */
  override def handleCommand(
    entityId: String,
    state: Option[Resource],
    command: ResourceCommand,
    replyTo: Option[ActorRef[ResourceResult]],
    timers: TimerScheduler[ResourceCommand]
  )(implicit
    context: ActorContext[ResourceCommand]
  ): Effect[ResourceEvent, Option[Resource]] =
    command match {

      case cmd: CreateResource =>
        import cmd._
        val createdDate = Instant.now()
        val resource =
          asResource(uuid, bytes, uri)
            .withCreatedDate(createdDate)
            .withLastUpdated(createdDate)
        val sessionId = if (uuid.contains('#')) uuid.split('#').headOption else None
        val sessionEvent = sessionId.map(sid =>
          SessionResourceUpsertedEvent.defaultInstance
            .withUuid(uuid)
            .withSessionId(sid)
            .withContent(resource.content)
            .copy(uri = uri)
        )
        Effect
          .persist(
            ResourceCreatedEvent(resource) +: sessionEvent.toList
          )
          .thenRun(_ => { ResourceCreated ~> replyTo })

      case cmd: UpdateResource =>
        import cmd._
        val lastUpdated = Instant.now()
        val createdDate = {
          state match {
            case Some(resource) => resource.createdDate
            case None           => Instant.now()
          }
        }
        val resource =
          asResource(uuid, bytes, uri)
            .withCreatedDate(createdDate)
            .withLastUpdated(lastUpdated)
        val sessionId = if (uuid.contains('#')) uuid.split('#').headOption else None
        val sessionEvent = sessionId.map(sid =>
          SessionResourceUpsertedEvent.defaultInstance
            .withUuid(uuid)
            .withSessionId(sid)
            .withContent(resource.content)
            .copy(uri = uri)
        )
        Effect
          .persist(
            ResourceUpdatedEvent(resource) +: sessionEvent.toList
          )
          .thenRun(_ => { ResourceUpdated ~> replyTo })

      case _: LoadResource =>
        state match {
          case Some(resource) => Effect.none.thenRun(_ => ResourceLoaded(resource) ~> replyTo)
          case _              => Effect.none.thenRun(_ => ResourceNotFound ~> replyTo)
        }

      case _: DeleteResource =>
        state match {
          case Some(resource) =>
            val uuid =
              resource.uri match {
                case Some(uri) => s"$uri/$entityId"
                case _         => entityId
              }
            val sessionId = if (uuid.contains('#')) uuid.split('#').headOption else None
            val sessionEvent = sessionId.map(sid =>
              SessionResourceDeletedEvent.defaultInstance.withUuid(uuid).withSessionId(sid)
            )
            Effect
              .persist[ResourceEvent, Option[Resource]](
                ResourceDeletedEvent(
                  uuid
                ) +: sessionEvent.toList
              )
              .thenRun(_ => {
                ResourceDeleted ~> replyTo
              })
              .thenStop()
          case _ => Effect.none.thenRun(_ => ResourceNotFound ~> replyTo)
        }

      case _ => super.handleCommand(entityId, state, command, replyTo, timers)
    }

  private[this] def asResource(uuid: String, bytes: Array[Byte], uri: Option[String]): Resource = {
    val mimetype =
      Try(new Tika().detect(bytes)) match {
        case Success(s) => Some(s)
        case Failure(_) => None
      }
    val content = Base64Tools.encodeBase64(bytes)
    val md5 = HashTools
      .hashStream(
        new ByteArrayInputStream(
          bytes
        )
      )
      .getOrElse("")
    Resource.defaultInstance
      .withUuid(uuid)
      .withContent(content)
      .withMd5(md5)
      .copy(mimetype = mimetype, uri = uri)
  }
}

object ResourceBehavior extends ResourceBehavior
