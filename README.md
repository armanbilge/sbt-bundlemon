# sbt-bundlemon

Track Scala.js bundle size in CI with [BundleMon](https://github.com/LironEr/bundlemon). Check out an [example PR](https://github.com/armanbilge/sbt-bundlemon/pull/7#issuecomment-1189183347).

## Configure

1. Install the [BundleMon App](https://github.com/apps/bundlemon) on your repository.

2. In `project/plugins.sbt` add:
```scala
addSbtPlugin("com.armanbilge" % "sbt-bundlemon" % "0.1.1")
```

3. Enable the plugin on one or more _applications_ in your `build.sbt` (or anything that [exports to JavaScript](https://www.scala-js.org/doc/interoperability/export-to-javascript.html)):
```scala
lazy val todoMvc = project.in(file("todo-mvc"))
  .enablePlugins(BundleMonPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true
  )
```

4. Add the following step to your CI workflow:
```yaml
- name: Monitor bundle size
  run: sbt bundleMon
```

5. Now you will get reports about the gzipped, fully-optimized bundle size in CI status and PR comments!

Please open issues and PRs for anything and everything :)
