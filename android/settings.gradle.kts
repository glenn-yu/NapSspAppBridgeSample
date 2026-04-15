pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")
        maven(url = "https://artifact.bytedance.com/repository/pangle/")
        // nap ssp vendor Maven repository placeholder.
        // Replace with the exact repository URL from the vendor guide.
        // maven(url = "<vendor-maven-repo-url>")
    }
}

rootProject.name = "NapSspAndroidSample"
include(":app")
