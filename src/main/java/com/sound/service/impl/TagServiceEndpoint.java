package com.sound.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/tag")
public class TagServiceEndpoint {

	@Autowired
	com.sound.service.sound.itf.TagService tagService;

	@Autowired
	SoundService soundService;

	@PUT
	@Path("/create/{userAlias}/{tag}")
	public Response createTag(
			@PathParam("tag") @NotNull String label, 
			@PathParam("userAlias") @NotNull String userAlias) 
	{
		try 
		{
			tagService.getOrCreate(label, userAlias);

		} 
		catch (SoundException e) 
		{
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage())
					.build();
		}
		catch (Exception e)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Failed to create tag " + label)
					.build();
		}
		
		return Response.status(Response.Status.CREATED)
				.entity("true").build();
	}

	@PUT
	@Path("/attach")
	public Response attachTagsToSound(
			@FormParam("soundAlias") @NotNull String soundAlias,
			@FormParam("tags") @NotNull List<String> tagLabels, 
			@FormParam("userAlias") @NotNull String userAlias) 
	{
		try 
		{
			tagService.attachToSound(soundAlias, tagLabels, userAlias);
		} catch (Exception e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot attach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("true").build();
	}

	@PUT
	@Path("/detach")
	public Response detachTagsFromSound(
			@FormParam("soundAlias") @NotNull String soundAlias,
			@FormParam("tags") @NotNull List<String> tagLabels, 
			@FormParam("userAlias") @NotNull String userAlias) {
		try 
		{
			tagService.detachFromSound(soundAlias, tagLabels, userAlias);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot detach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("true").build();
	}

	@GET
	@Path("/match/{pattern}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTagsContains(
			@PathParam("pattern") @NotNull String pattern) 
	{
		List<String> tagLabels = null;
		try {
			List<Tag> tags = tagService.listTagsContains(pattern);
			tagLabels = new ArrayList<String>();
			for (Tag tag : tags) {
				tagLabels.add(tag.getLabel());
			}
			
		} catch (SoundException e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot detach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity(tagLabels).build();
	}

	@GET
	@Path("/sounds/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoundsByTag(
			@PathParam("label") @NotNull String tagLabel) 
	{
		List<Sound> sounds = null;
		try {
			sounds = tagService.getSoundsWithTag(tagLabel);
		} catch (SoundException e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot get sounds by tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity(sounds.toString()).build();
	}

}
