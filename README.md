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
	implementation("com.github.WhichWho:kemono-api:+")
}
```
