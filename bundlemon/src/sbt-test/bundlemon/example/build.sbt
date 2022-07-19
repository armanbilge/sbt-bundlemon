name := "sbt-bundlemon-scripted-example"
enablePlugins(BundleMonPlugin)
scalaJSUseMainModuleInitializer := true
bundleMonCheckRun := true
bundleMonCompression := BundleMonCompression.Brotli
