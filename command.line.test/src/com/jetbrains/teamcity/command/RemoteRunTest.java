package com.jetbrains.teamcity.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import jetbrains.buildServer.util.FileUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jetbrains.teamcity.TestServer;
import com.jetbrains.teamcity.TestingUtil;
import com.jetbrains.teamcity.resources.ITCResourceMatcher;
import com.jetbrains.teamcity.resources.TCWorkspace;
import com.jetbrains.teamcity.runtime.NullProgressMonitor;

public class RemoteRunTest {
	
	private static RemoteRun ourCommand;
	private static File ourRootFolder;
	private static String ourCurrentDirectory;
	private static TestServer ourTestServer;

	@BeforeClass
	public static void setup() throws Exception {
		//dirs
		ourRootFolder = TestingUtil.createFS();
		
		//keep current directory
		ourCurrentDirectory = System.getProperty("user.dir");
		//change current directory
		System.setProperty("user.dir", ourRootFolder.getCanonicalFile().getAbsolutePath()); 
		
		ourCommand = new RemoteRun();
		
		ourTestServer = new TestServer();
	}
	
	@AfterClass
	public static void clean() throws Exception {
		System.setProperty("user.dir", ourCurrentDirectory);
		TestingUtil.releaseFS(ourRootFolder);
	}
	
	@Test
	public void getApplicableConfigurations() {
		//TODO: implement it
	}
	
	@Test
	public void createPatch() {
		//TODO: implement it
	}
	
	@Test
	public void createChangeList() {
		//TODO: implement it
	}
	
	@Test
	public void TW_9694() throws Exception {
		final File configFile = new File(ourRootFolder + File.separator + "java" + File.separator + "resources", TCWorkspace.TCC_ADMIN_FILE);
		FileUtil.writeFile(configFile, ".=//depo/test/resources\n");
		final File patchFile = new File("./test.patch");
		patchFile.createNewFile();
		
		try{
			//file under TC
			final File controlledFile = new File("java" + File.separator + "resources", "java.resources");
			File patch = ourCommand.createPatch(patchFile, new TCWorkspace(new File(".")), Collections.singletonList(controlledFile));
			assertTrue(patch.length() > 10);
			//file is not under TC: disable default config
			File uncontrolledFile = new File("java", "1.java");
			patch = ourCommand.createPatch(patchFile, new TCWorkspace(new File(".")){
				@Override
				protected File getGlobalAdminFile() {
					return null;
				}
			}, Arrays.asList(new File[] {controlledFile, uncontrolledFile}));
			assertTrue(patch.length() > 10);
		} finally {
			FileUtil.delete(patchFile);
			FileUtil.delete(configFile);
		}
	}
	
	@Test
	public void fireRemoteRun() {
		//TODO: implement it
	}
		
	@Test
	public void waitForSuccessResult() {
		//TODO: implement it
	}
	
	@Test
	public void getFiles_file_list_passed() {
		//TODO: implement it
	}
	
	@Test
	public void getFiles_file_target_passed() {
		//TODO: implement it		
	}

	@Test
	public void getFiles_nothing_passed() {
		final Collection<File> files = ourCommand.getFiles(new Args(new String[] {RemoteRun.NO_WAIT_SWITCH_LONG, RemoteRun.CONFIGURATION_PARAM_LONG, "bt2"}), new NullProgressMonitor());
		assertNotNull("null file's collection got", files);
		assertEquals("wrong files count collected", 5, files.size());
	}
	
	@Test
	public void validate_error() {
		try{
			ourCommand.validate(null);
			assertTrue("Exception expected", true);
		} catch (IllegalArgumentException e){
			//ok
		}
		try{
			ourCommand.validate(new Args(new String[]{}));
			assertTrue("Exception expected", true);
		} catch (IllegalArgumentException e){
			//ok
		}
		
	}
	
	@Test
	public void validate_ok() {
		ourCommand.validate(new Args(new String[] { RemoteRun.ID, RemoteRun.MESSAGE_PARAM, "short" }));
		ourCommand.validate(new Args(new String[] { RemoteRun.ID, RemoteRun.MESSAGE_PARAM_LONG, "long" }));
		ourCommand.validate(new Args(new String[] { RemoteRun.ID, RemoteRun.MESSAGE_PARAM, "short", RemoteRun.MESSAGE_PARAM_LONG, "long" }));
	}
	
	@Test
	public void getGlobalConfigFile() throws Exception {
		
		//null on null
		ITCResourceMatcher config = ourCommand.getOverridingMatcher(new Args(new String[] {RemoteRun.ID}));
		assertNull(config);
		
		//error on virtual file 
		try{
			config = ourCommand.getOverridingMatcher(new Args(new String[] {RemoteRun.ID, RemoteRun.OVERRIDING_MAPPING_FILE_PARAM, "global_config"}));
			assertTrue(false); //should not be here
		} catch (IllegalArgumentException e){
			//OK: file is not exist
		}
		
		//wrong format
		File configFile = new File(ourCurrentDirectory, "global_config");
		configFile.createNewFile();
		try{
			config = ourCommand.getOverridingMatcher(new Args(new String[] {RemoteRun.ID, RemoteRun.OVERRIDING_MAPPING_FILE_PARAM, configFile.getAbsolutePath()}));
			assertTrue(false); //should not be here
		} catch (IllegalArgumentException e){
			//OK: file format is wrong
		} finally {
			FileUtil.delete(configFile);
		}
		
		//all ok
		configFile = new File(ourCurrentDirectory, "global_config");
		try{
			FileUtil.writeFile(configFile, ".=//depo/test/\n");
			config = ourCommand.getOverridingMatcher(new Args(new String[] {RemoteRun.ID, RemoteRun.OVERRIDING_MAPPING_FILE_PARAM, configFile.getAbsolutePath()}));
			assertNotNull(config);
		} finally {
			FileUtil.delete(configFile);
		}
		
	}
	
	
	
	
}
