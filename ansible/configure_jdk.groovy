// import jenkins.model.*
// import hudson.plugins.sonar.*
// import hudson.plugins.sonar.model.TriggersConfig
// import hudson.tools.*
// import com.cloudbees.plugins.credentials.CredentialsProvider
// import com.cloudbees.plugins.credentials.common.StandardCredentials

// // Required environment variables
// def sonar_name = "SonarQube"
// def sonar_server_url = "http://sonarqube:9000"
// def sonar_auth_token = getSonarQubeAuthToken("sonarqube-token")?.getPlainText() ?: ""

// // Check if the token is retrieved
// if (sonar_auth_token.isEmpty()) {
//     println("Warning: SonarQube token not found in global secrets. Please check the configuration.")
//     return
// }

// def sonar_mojo_version = ''
// def sonar_additional_properties = ''
// def sonar_triggers = new TriggersConfig()
// def sonar_additional_analysis_properties = ''


// // Define SONAR_RUNNER_HOME
// def sonar_runner_home = "/opt/sonar-scanner/bin"

// // Get the Jenkins instance
// def instance = Jenkins.getInstance()

// Thread.start {
//     sleep(10000)
//     println("Configuring SonarQube...")

//     // Get the GlobalConfiguration descriptor of SonarQube plugin
//     def sonar_conf = instance.getDescriptor(SonarGlobalConfiguration.class)

//     def sonar_inst = new SonarInstallation(
//         sonar_name,
//         sonar_server_url,
//         sonar_auth_token,
//         sonar_mojo_version,
//         sonar_additional_properties,
//         sonar_triggers,
//         sonar_additional_analysis_properties
//     )

//     // Only add the new Sonar setting if it does not exist - do not overwrite existing config
//     def sonar_installations = sonar_conf.getInstallations()
//     def sonar_inst_exists = sonar_installations.any { it.getName() == sonar_name }

//     if (!sonar_inst_exists) {
//         sonar_installations += sonar_inst
//         sonar_conf.setInstallations((SonarInstallation[]) sonar_installations)
//         sonar_conf.save()
//     }

//     // Step 2 - Configure SonarRunner without automatic installation
//     println("Configuring SonarRunner...")
//     def desc_SonarRunnerInst = instance.getDescriptor("hudson.plugins.sonar.SonarRunnerInstallation")

//     // Create Sonar Runner installation without automatic installer
//     def sonarRunner_inst = new SonarRunnerInstallation("sonar-scanner", sonar_runner_home, [])

//     // Only add our Sonar Runner if it does not exist - do not overwrite existing config
//     def sonar_runner_installations = desc_SonarRunnerInst.getInstallations()
//     def sonar_runner_inst_exists = sonar_runner_installations.any { it.getName() == sonarRunner_inst.getName() }

//     if (!sonar_runner_inst_exists) {
//         sonar_runner_installations += sonarRunner_inst
//         desc_SonarRunnerInst.setInstallations((SonarRunnerInstallation[]) sonar_runner_installations)
//         desc_SonarRunnerInst.save()
//     }

//     // JDK installation
//     def jdk = new JDK("JDK 17", "/usr/lib/jvm/java-17-openjdk-amd64")
//     // Get the JDK descriptor
//     def jdkDescriptor = instance.getDescriptorByType(JDK.DescriptorImpl.class)
//     // Get existing JDK installations
//     def jdkInstallations = jdkDescriptor.getInstallations()
//     // Add new JDK to installations array
//     jdkInstallations += jdk
//     // Update the installations
//     jdkDescriptor.setInstallations(jdkInstallations as JDK[])

//     // Save the state
//     instance.save()
//     println "JDK 17 and SonarQube Scanner have been configured successfully."
// }

// // Function to retrieve SonarQube auth token from Jenkins global secrets
// def getSonarQubeAuthToken(String secretId) {
//     def credentials = CredentialsProvider.lookupCredentials(
//         StandardCredentials.class,
//         Jenkins.instance,
//         null,
//         null
//     )

//     def secret = credentials.find { it.id == secretId }
//     return secret?.secret ?: null
// }


import jenkins.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.TriggersConfig
import hudson.tools.*
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials

// Required environment variables
def sonar_name = "SonarQube"
def sonar_server_url = "http://{{ ansible_host }}:9000"  // Use the dynamic Ansible host for SonarQube server
def sonar_auth_token = getSonarQubeAuthToken("sonarqube-token")?.getPlainText() ?: ""

// Check if the token is retrieved
if (sonar_auth_token.isEmpty()) {
    println("Warning: SonarQube token not found in global secrets. Please check the configuration.")
    return
}

def sonar_mojo_version = ''
def sonar_additional_properties = ''
def sonar_triggers = new TriggersConfig()
def sonar_additional_analysis_properties = ''

// Define SONAR_RUNNER_HOME
def sonar_runner_home = "/opt/sonar-scanner/bin"

// Get the Jenkins instance
def instance = Jenkins.getInstance()

Thread.start {
    sleep(10000)
    println("Configuring SonarQube...")

    // Get the GlobalConfiguration descriptor of SonarQube plugin
    def sonar_conf = instance.getDescriptor(SonarGlobalConfiguration.class)

    def sonar_inst = new SonarInstallation(
        sonar_name,
        sonar_server_url,
        sonar_auth_token,
        sonar_mojo_version,
        sonar_additional_properties,
        sonar_triggers,
        sonar_additional_analysis_properties
    )

    // Only add the new Sonar setting if it does not exist - do not overwrite existing config
    def sonar_installations = sonar_conf.getInstallations()
    def sonar_inst_exists = sonar_installations.any { it.getName() == sonar_name }

    if (!sonar_inst_exists) {
        sonar_installations += sonar_inst
        sonar_conf.setInstallations((SonarInstallation[]) sonar_installations)
        sonar_conf.save()
    }

    // Step 2 - Configure SonarRunner without automatic installation
    println("Configuring SonarRunner...")
    def desc_SonarRunnerInst = instance.getDescriptor("hudson.plugins.sonar.SonarRunnerInstallation")

    // Create Sonar Runner installation without automatic installer
    def sonarRunner_inst = new SonarRunnerInstallation("sonar-scanner", sonar_runner_home, [])

    // Only add our Sonar Runner if it does not exist - do not overwrite existing config
    def sonar_runner_installations = desc_SonarRunnerInst.getInstallations()
    def sonar_runner_inst_exists = sonar_runner_installations.any { it.getName() == sonarRunner_inst.getName() }

    if (!sonar_runner_inst_exists) {
        sonar_runner_installations += sonarRunner_inst
        desc_SonarRunnerInst.setInstallations((SonarRunnerInstallation[]) sonar_runner_installations)
        desc_SonarRunnerInst.save()
    }

    // JDK installation
    def jdk = new JDK("JDK 17", "/usr/lib/jvm/java-17-openjdk-amd64")
    // Get the JDK descriptor
    def jdkDescriptor = instance.getDescriptorByType(JDK.DescriptorImpl.class)
    // Get existing JDK installations
    def jdkInstallations = jdkDescriptor.getInstallations()
    // Add new JDK to installations array
    jdkInstallations += jdk
    // Update the installations
    jdkDescriptor.setInstallations(jdkInstallations as JDK[])

    // Save the state
    instance.save()
    println "JDK 17 and SonarQube Scanner have been configured successfully."
}

// Function to retrieve SonarQube auth token from Jenkins global secrets
def getSonarQubeAuthToken(String secretId) {
    def credentials = CredentialsProvider.lookupCredentials(
        StandardCredentials.class,
        Jenkins.instance,
        null,
        null
    )

    def secret = credentials.find { it.id == secretId }
    return secret?.secret ?: null
}
