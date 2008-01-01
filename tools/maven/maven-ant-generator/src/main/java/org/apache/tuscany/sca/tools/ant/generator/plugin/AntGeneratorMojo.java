/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.tuscany.sca.tools.ant.generator.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * @version $Rev$ $Date$
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution test
 * @description Generate Ant build script for an SCA project
 */
public class AntGeneratorMojo extends AbstractMojo {
    /**
     * The project to create a build for.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Used for resolving artifacts
     *
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * Factory for creating artifact objects
     *
     * @component
     */
    private ArtifactFactory factory;

    /**
     * The local repository where the artifacts are located
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * The remote repositories where artifacts are located
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List remoteRepositories;

    /**
     * The current user system settings for use in Maven.
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * The main class name.
     * @parameter
     */
    private String mainClass;
    
    /**
     * If set true then only the dependency file is created. The dependency
     * file can then be included in a hand generated build.xml file
     * @parameter
     */
    private Boolean buildDependencyFileOnly;    
    
    /**
     * The build.xml file to generate.
     * @parameter expression="${basedir}/build.xml"
     */
    private String buildFile;
    
    /**
     * The build-dependency.xml file to generate.
     * @parameter expression="${basedir}/build-dependency.xml"
     */
    private String buildDependencyFile;    
    
    public void execute() throws MojoExecutionException {
        if ((buildDependencyFileOnly != null) &&
            (buildDependencyFileOnly == true)){
            createBuildDependencyXML();
        } else {
            createBuildXML();
        }
    }
    
    public void createBuildDependencyXML() throws MojoExecutionException {
        
        getLog().info("Generating " + buildDependencyFile);
        
        // Open the target build-dependency.xml file
        File targetFile = new File(buildDependencyFile);
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileOutputStream(targetFile));
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.toString());
        }
        

        // Determine the project packaging
        String packaging = project.getPackaging().toLowerCase();

        // Determine the module dependencies
        List<Artifact> tuscanyModules = new ArrayList<Artifact>();
        List<Artifact> otherModules = new ArrayList<Artifact>();
        for (Artifact artifact: (List<Artifact>)project.getRuntimeArtifacts()) {
            if (artifact.getGroupId().startsWith("org.apache.tuscany.sca")) {
                tuscanyModules.add(artifact);
            } else {
                otherModules.add(artifact);
            }
        }
        
        pw.println("<!--");
        pw.println(" * Licensed to the Apache Software Foundation (ASF) under one");
        pw.println(" * or more contributor license agreements.  See the NOTICE file");
        pw.println(" * distributed with this work for additional information");
        pw.println(" * regarding copyright ownership.  The ASF licenses this file");
        pw.println(" * to you under the Apache License, Version 2.0 (the");
        pw.println(" * \"License\"); you may not use this file except in compliance");
        pw.println(" * with the License.  You may obtain a copy of the License at");
        pw.println(" * ");
        pw.println(" *   http://www.apache.org/licenses/LICENSE-2.0");
        pw.println(" * ");
        pw.println(" * Unless required by applicable law or agreed to in writing,");
        pw.println(" * software distributed under the License is distributed on an");
        pw.println(" * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY");
        pw.println(" * KIND, either express or implied.  See the License for the");
        pw.println(" * specific language governing permissions and limitations");
        pw.println(" * under the License.");
        pw.println("-->");

        pw.println();
    
        // Generate the classpath
        pw.println("    <fileset id=\"tuscany.jars\" dir=\"../../modules\">");
        for (Artifact artifact: tuscanyModules) {
            pw.println("        <include name=\"" + artifact.getFile().getName() +"\"/>");
        }
        pw.println("    </fileset>");
        
        pw.println("    <fileset id=\"3rdparty.jars\" dir=\"../../lib\">");
        for (Artifact artifact: otherModules) {
            pw.println("        <include name=\"" + artifact.getFile().getName() +"\"/>");
        }
        pw.println("    </fileset>");
        
        pw.println();
        
        pw.close();
    }
    
    public void createBuildXML() throws MojoExecutionException {
        
        getLog().info("Generating " + buildFile);
        
        // Open the target build.xml file
        File targetFile = new File(buildFile);
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileOutputStream(targetFile));
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.toString());
        }
        

        // Determine the project packaging
        String packaging = project.getPackaging().toLowerCase();

        // Determine the module dependencies
        List<Artifact> tuscanyModules = new ArrayList<Artifact>();
        List<Artifact> otherModules = new ArrayList<Artifact>();
        for (Artifact artifact: (List<Artifact>)project.getRuntimeArtifacts()) {
            if (artifact.getGroupId().startsWith("org.apache.tuscany.sca")) {
                tuscanyModules.add(artifact);
            } else {
                otherModules.add(artifact);
            }
        }
        
        pw.println("<!--");
        pw.println(" * Licensed to the Apache Software Foundation (ASF) under one");
        pw.println(" * or more contributor license agreements.  See the NOTICE file");
        pw.println(" * distributed with this work for additional information");
        pw.println(" * regarding copyright ownership.  The ASF licenses this file");
        pw.println(" * to you under the Apache License, Version 2.0 (the");
        pw.println(" * \"License\"); you may not use this file except in compliance");
        pw.println(" * with the License.  You may obtain a copy of the License at");
        pw.println(" * ");
        pw.println(" *   http://www.apache.org/licenses/LICENSE-2.0");
        pw.println(" * ");
        pw.println(" * Unless required by applicable law or agreed to in writing,");
        pw.println(" * software distributed under the License is distributed on an");
        pw.println(" * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY");
        pw.println(" * KIND, either express or implied.  See the License for the");
        pw.println(" * specific language governing permissions and limitations");
        pw.println(" * under the License.");
        pw.println("-->");

        
        pw.println("<project name=\"" + project.getArtifactId() + "\" default=\"compile\">");
        pw.println();
        
        // Generate the compile target
        int base = project.getBasedir().toString().length() + 1;
        pw.println("    <target name=\"compile\">");
        pw.println("        <mkdir dir=\"target/classes\"/>");
        pw.println("        <javac destdir=\"target/classes\" debug=\"on\" source=\"1.5\" target=\"1.5\">");
        for (String source: (List<String>)project.getCompileSourceRoots()) {
            if (source.length() > base) {
                source = source.substring(base);
            } else {
                source = ".";
            }
            pw.println("            <src path=\"" + source + "\"/>");
        }
        pw.println("            <classpath>");
        pw.println("                <fileset refid=\"tuscany.jars\"/>");
        pw.println("                <fileset refid=\"3rdparty.jars\"/>");
        pw.println("            </classpath>");
        pw.println("        </javac>");
        pw.println("        <copy todir=\"target/classes\">");
        for (FileSet resource: (List<FileSet>)project.getResources()) {
            String source = resource.getDirectory();
            if (source.length() > base) {
                source = source.substring(base);
                
                if (source.equals(".")){
                    pw.println("            <fileset dir=\".\" includes=\"*\" excludes=\"src, target, pom.xml, build.xml\"/>");
                } else {
                    pw.println("            <fileset dir=\"" + source + "\"/>");
                }
            } else {
                if (project.getResources().size() > 1) {
                    break;
                }
                pw.println("            <fileset dir=\".\" excludes=\"**/*.java, pom.xml, build.xml, target\"/>");
                source = ".";
            }
        }
        pw.println("        </copy>");
        
        // Build a JAR
        if (packaging.equals("jar")) {
            pw.println("        <jar destfile=\"target/" + project.getArtifactId() + ".jar\" basedir=\"target/classes\">");
            pw.println("            <manifest>");
            if (mainClass != null) {
                pw.println("                <attribute name=\"Main-Class\" value=\"" + mainClass + "\"/>");
            }
            pw.println("            </manifest>");
            pw.println("        </jar>");
            
        } else if (packaging.equals("war")) {
            
            // Build a WAR
            pw.println("        <war destfile=\"target/" + project.getArtifactId() + ".war\" webxml=\"src/main/webapp/WEB-INF/web.xml\">");
            pw.println("            <fileset dir=\"src/main/webapp\"/>");
            pw.println("            <lib refid=\"tuscany.jars\"/>");
            pw.println("            <lib refid=\"3rdparty.jars\"/>");
            pw.println("            <classes dir=\"target/classes\"/>");
            pw.println("        </war>");
        }
        pw.println("    </target>");
        pw.println();
    
        // Generate the run target
        if (mainClass != null) {
            pw.println("    <target name=\"run\">");
            pw.println("        <java classname=\"" + mainClass + "\" fork=\"true\">");
            pw.println("            <classpath>");
            pw.println("                <pathelement location=\"target/" + project.getArtifactId() + ".jar\"/>");
            pw.println("                <fileset refid=\"tuscany.jars\"/>");
            pw.println("                <fileset refid=\"3rdparty.jars\"/>");
            pw.println("            </classpath>");
            pw.println("        </java>");
            pw.println("    </target>");
            pw.println();
        }
        
        // Generate the clean target
        pw.println("    <target name=\"clean\">");
        pw.println("        <delete includeemptydirs=\"true\">");
        pw.println("            <fileset dir=\"target\"/>");
        pw.println("        </delete>");
        pw.println("    </target>");
        pw.println();
    
        // Generate the classpath
        pw.println("    <fileset id=\"tuscany.jars\" dir=\"../../modules\">");
        for (Artifact artifact: tuscanyModules) {
            pw.println("        <include name=\"" + artifact.getFile().getName() +"\"/>");
        }
        pw.println("    </fileset>");
        pw.println("    <fileset id=\"3rdparty.jars\" dir=\"../../lib\">");
        for (Artifact artifact: otherModules) {
            pw.println("        <include name=\"" + artifact.getFile().getName() +"\"/>");
        }
        pw.println("    </fileset>");
        pw.println();
        
        pw.println("</project>");
        pw.close();
    }
    

}