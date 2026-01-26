import mill._
import mill.scalalib._
import mill.scalalib.publish._

object `golem-java` extends JavaModule with PublishModule {

  def publishVersion = "0.0.0"

  def pomSettings = PomSettings(
    description = "Java SDK for building Golem WebAssembly components",
    organization = "cloud.golem",
    url = "https://github.com/golemcloud/golem",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("golemcloud", "golem"),
    developers = Seq(
      Developer("golemcloud", "Golem Cloud", "https://golem.cloud")
    )
  )

  def artifactName = "golem-java"

  // Java 17
  def javacOptions = Seq("-source", "17", "-target", "17", "-Xlint:all")

  // TeaVM dependencies for WASM compilation
  def ivyDeps = Agg(
    ivy"org.teavm:teavm-classlib:0.10.0",
    ivy"org.teavm:teavm-interop:0.10.0"
  )

  // Test dependencies
  object test extends JavaTests with TestModule.Junit5 {
    def ivyDeps = Agg(
      ivy"org.junit.jupiter:junit-jupiter:5.10.1"
    )
  }

  // Task to compile to WASM using TeaVM
  def compileWasm = T {
    val classFiles = compile()
    val cp = runClasspath().map(_.path)

    // TeaVM compiler invocation
    os.proc(
      "java",
      "-cp", cp.mkString(":"),
      "org.teavm.cli.TeaVMRunner",
      "--targettype", "WEBASSEMBLY",
      "--targetdir", (T.dest / "wasm").toString,
      "--mainclass", "cloud.golem.ComponentMain",
      "--optimization", "ADVANCED",
      "--debug"
    ).call(cwd = T.dest)

    PathRef(T.dest / "wasm" / "classes.wasm")
  }

  // Task to create WASM component
  def componentize = T {
    val wasmFile = compileWasm()

    os.proc(
      "wasm-tools",
      "component", "new",
      wasmFile.path.toString,
      "-o", (T.dest / "golem-java.wasm").toString,
      "--adapt", "wasi_snapshot_preview1=adapters/wasi_snapshot_preview1.wasm"
    ).call(cwd = millSourcePath)

    PathRef(T.dest / "golem-java.wasm")
  }
}
