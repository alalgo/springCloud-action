package org.alalgo.bpm.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/activiti")
public class ActivitiController {
	@Autowired
	private RepositoryService repositoryService;
	/**
	 * 新建一个空模型
	 * @author xujiaxin
	 * @version v1.0
	 */
	@RequestMapping("/create")
	public void create(HttpServletRequest request, HttpServletResponse response) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			
			//初始化一个空模型
			Model modelData = repositoryService.newModel();
			ObjectNode modelObjectNode = objectMapper.createObjectNode();
			modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "hello1111");
			modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "hello1111");
			modelData.setMetaInfo(modelObjectNode.toString());
			modelData.setName("hello1111");
			modelData.setKey("12313123"); 
			repositoryService.saveModel(modelData);
			
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.put("stencilset", stencilSetNode);
			repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
			
			response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
		} catch (Exception e) {
			System.out.println("创建模型失败：");
		}
	}
	
	/**
     * 获取所有模型
     * @return
     */
    @GetMapping("/list")
    public List<Model> modelList(){
        List<Model> models = repositoryService.createModelQuery().list();
        return models;
    }
    /**
     * 删除模型
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public String deleteModel(@PathVariable("id")String id){
        repositoryService.deleteModel(id);
        return "delete sucess.";
    }
    
    /**
     * 发布模型为流程定义
     * @param id
     * @return
     * @throws Exception
     */
    @PostMapping("/deployment/{id}")
    public String deploy(@PathVariable("id")String id) throws Exception {
    	
        //获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return "数据模型不符要求，请至少设计一条主线流程。";
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        return "发布成功。";
    }
}    

