# cloud-api
Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url = uri("https://jitpack.io") }
	}
}
```
Step 2. Add the dependency
```
dependencies {
    val cloudApiVersion = "3.0.0"
    implementation("com.github.WhichWho.cloud-api:core:$cloudApiVersion")
    implementation("com.github.WhichWho.cloud-api:onedrive:$cloudApiVersion")
    implementation("com.github.WhichWho.cloud-api:googledrive:$cloudApiVersion")
    implementation("com.github.WhichWho.cloud-api:zipfile:$cloudApiVersion")
    implementation("com.github.WhichWho.cloud-api:localfile:$cloudApiVersion")
}
```
