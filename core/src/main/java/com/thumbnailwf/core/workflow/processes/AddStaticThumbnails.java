package com.thumbnailwf.core.workflow.processes;

import java.util.HashMap;
import java.util.Objects;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.asset.api.AssetManager;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

@Component(service = WorkflowProcess.class, property = "process.label=Add Static Thumbnail Workflow Process")
public class AddStaticThumbnails implements WorkflowProcess {

	private static final Logger LOG = LoggerFactory.getLogger(AddStaticThumbnails.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	private static final String RENDITIONS_PATH = "/jcr:content/renditions";
	private static final String ORIGINAL= "/original";

	@Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
		// TODO Auto-generated method stub

		LOG.debug("## Add static thumbnails workflow step ##");

		String payloadPath = item.getContentPath();
		String thumbnailPath = args.get("CONFIGS", null);

		LOG.debug("Target asset path: {}", payloadPath);
		LOG.debug("Thumbnail path: {}", thumbnailPath);
		ResourceResolver resourceResolver = null;
		try {
			resourceResolver = getResourceResolver();
			String sourcePath = thumbnailPath.concat(RENDITIONS_PATH);
			String destPath = payloadPath.replace(ORIGINAL, "");
			copyThumbnails(resourceResolver, sourcePath, destPath);
			resourceResolver.commit();
			LOG.debug("Thumbnails moved to path: {}", destPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("{}: Exception occured while moving assets: {}", this.getClass().getName(), e.getMessage());
		} finally {
			if (Objects.nonNull(resourceResolver)) {
				resourceResolver.close();
			}
		}
	}

	private ResourceResolver getResourceResolver() throws LoginException {
		HashMap<String, Object> serviceParam = new HashMap<String, Object>();
		// Replace it with your sub service name
		// Requires write access under /content/dam
		serviceParam.put(ResourceResolverFactory.SUBSERVICE, "damWriterService");
		ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(serviceParam);
		return resourceResolver;
	}

	private void copyThumbnails(ResourceResolver resourceResolver, String sourcePath, String destPath) {
		AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
		assetManager.copyAsset(sourcePath, destPath);
	}
}
