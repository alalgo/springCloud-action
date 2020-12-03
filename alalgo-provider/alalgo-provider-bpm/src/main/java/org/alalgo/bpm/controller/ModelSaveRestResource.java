package org.alalgo.bpm.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/service")
@Slf4j
public class ModelSaveRestResource implements ModelDataJsonConstants {


	  @Autowired
	  private RepositoryService repositoryService;
	  
	  @Autowired
	  private ObjectMapper objectMapper;

	  @RequestMapping(value="/model/{modelId}/save", method = RequestMethod.PUT)
	  @ResponseStatus(value = HttpStatus.OK)
	  public void saveModel(@PathVariable String modelId, @RequestParam("name") String name,
	                        @RequestParam("json_xml") String json_xml, @RequestParam("svg_xml") String svg_xml,
	                        @RequestParam("description") String description) {//对接收参数进行了修改
	    try {

	      Model model = repositoryService.getModel(modelId);

	      ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

	      modelJson.put(MODEL_NAME, name);
	      modelJson.put(MODEL_DESCRIPTION, description);
	      model.setMetaInfo(modelJson.toString());
	      model.setName(name);

	      repositoryService.saveModel(model);

	      repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));

	      InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
	      TranscoderInput input = new TranscoderInput(svgStream);

	      PNGTranscoder transcoder = new PNGTranscoder();
	      // Setup output
	      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	      TranscoderOutput output = new TranscoderOutput(outStream);

	      // Do the transformation
	      transcoder.transcode(input, output);
	      final byte[] result = outStream.toByteArray();
	      repositoryService.addModelEditorSourceExtra(model.getId(), result);
	      outStream.close();


	    } catch (Exception e) {
	      log.error("Error saving model", e);
	      throw new ActivitiException("Error saving model", e);
	    }
	}
}
