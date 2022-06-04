plugins{
    kotlin("jvm") version "1.6.21" apply false
    kotlin("plugin.spring") version "1.6.21" apply false
}

abstract class BuildAllTask: DefaultTask(){
    @TaskAction
    fun build(){

    }
}

tasks.register<Copy>("buildAll"){
    dependsOn(":shard:bootJar")
    dependsOn(":balancer:bootJar")
    dependsOn(":tester:shadowJar")
    from(file("tester/build/libs/tester-1.0-SNAPSHOT-all.jar"), file("balancer/build/libs/balancer-0.0.1-SNAPSHOT.jar"), file("shard/build/libs/shard-0.0.1-SNAPSHOT.jar"))
    into(".")
    rename {
        it.split("-")[0] + ".jar"
    }
}