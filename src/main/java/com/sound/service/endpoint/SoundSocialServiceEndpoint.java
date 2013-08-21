package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.service.sound.itf.SoundSocialService;
import com.sound.util.JsonHandler;

@Component
@Path("/soundActivity")
@RolesAllowed({Constant.ADMIN_ROLE,Constant.USER_ROLE})
public class SoundSocialServiceEndpoint {

  Logger logger = Logger.getLogger(SoundSocialServiceEndpoint.class);

  @Autowired
  SoundSocialService soundSocialService;

  @PUT
  @Path("/{userAlias}/play/{soundAlias}")
  public Response play(
      @NotNull @PathParam("userAlias") String userAlias,
      @NotNull @PathParam("soundAlias") String soundAlias
      ) {
    Map<String, String> result = null;
    try {
      result = soundSocialService.play(soundAlias, userAlias);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to record play sound " + soundAlias)).build();
    }
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/{userAlias}/like/{soundAlias}")
  public Response like(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @PathParam("userAlias") String userAlias) {
    Integer liked = 0;
    try {
      liked = soundSocialService.like(soundAlias, userAlias);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to like sound " + soundAlias)).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("liked", liked);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/{userAlias}/like/{soundAlias}")
  public Response unlike(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @PathParam("userAlias") String userAlias) {
    Integer liked = 0;
    try {
      liked = soundSocialService.dislike(soundAlias, userAlias);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to unlike sound " + soundAlias)).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("liked", liked);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/{userAlias}/repost/{soundAlias}")
  public Response repost(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @PathParam("userAlias") String userAlias) {
    Integer reposted = 0;
    try {
      reposted = soundSocialService.repost(soundAlias, userAlias);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to repost sound " + soundAlias)).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("reposted", reposted);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/{userAlias}/repost/{soundAlias}")
  public Response unrepost(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @PathParam("userAlias") String userAlias) {
    Integer reposted = 0;
    try {
      reposted = soundSocialService.unrepost(soundAlias, userAlias);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to unlike sound " + soundAlias)).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("reposted", reposted);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/{userAlias}/comment/{soundAlias}")
  public Response comment(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @PathParam("userAlias") String userAlias,
      @NotNull @QueryParam("comment") String comment, @QueryParam("pointAt") Float pointAt) {
    Integer commentsCount = 0;
    try {
      commentsCount = soundSocialService.comment(soundAlias, userAlias, comment, pointAt);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to comment on sound " + soundAlias)).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("commentsCount", commentsCount);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/comment/{commentId}")
  public Response comment(@NotNull @PathParam("commentId") String commentId) {
    Integer commentsCount = 0;
    try {
      commentsCount = soundSocialService.uncomment(commentId);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to comment with id " + commentId)).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("commentsCount", commentsCount);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @GET
  @Path("/{soundAlias}/comments")
  public Response comment(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @QueryParam("pageNum") Integer pageNum,
      @QueryParam("commentsPerPage") Integer commentsPerPage) {
    List<SoundComment> comments = null;
    try {
      comments = soundSocialService.getComments(soundAlias, pageNum, commentsPerPage);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to load comments of sound " + soundAlias)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(comments)).build();
  }

  @POST
  @Path("/recommand/sounds")
  public Response getRecommandedGroupsByTags(@NotNull @FormParam("tags") List<String> tags,
      @NotNull @FormParam("pageNum") Integer pageNum,
      @NotNull @FormParam("pageSize") Integer pageSize) {
    List<Sound> sounds = new ArrayList<Sound>();
    try {
      sounds.addAll(soundSocialService.recommandSoundsByTags(tags, pageNum, pageSize));
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
    return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
  }
}