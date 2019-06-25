load("@bazel_skylib//lib:shell.bzl", "shell")

def _gofmt_impl(ctx):
    # Taken from https://github.com/bazelbuild/buildtools/blob/bf564b4925ab5876a3f64d8b90fab7f769013d42/buildifier/def.bzl#L15
    exclude_patterns_str = ""
    if ctx.attr.exclude_patterns:
        exclude_patterns = ["-not -path %s" % shell.quote(pattern) for pattern in ctx.attr.exclude_patterns]
        exclude_patterns_str = " ".join(exclude_patterns) + " -and"

    out_file = ctx.actions.declare_file(ctx.label.name + ".bash")
    substitutions = {
        "@@EXCLUDE_PATTERNS@@": exclude_patterns_str,
        "@@MODE@@": ctx.attr.mode,
    }

    ctx.actions.expand_template(
        template = ctx.file._runner,
        output = out_file,
        substitutions = substitutions,
        is_executable = True,
    )
    runfiles = ctx.runfiles(files = ctx.files._go_sdk_tools)
    return [DefaultInfo(
        runfiles = runfiles,
        executable = out_file,
    )]

gofmt = rule(
    implementation = _gofmt_impl,
    attrs = {
        "mode": attr.string(
            values = [
                "format",
                "check",
            ],
            default = "format",
        ),
        "exclude_patterns": attr.string_list(
            allow_empty = True,
            doc = "A list of glob patterns passed to the find command. E.g. './vendor/*' to exclude the Go vendor directory",
            default = [
                ".*.git/*",
                ".*.project/*",
                ".*idea/*",
            ],
        ),
        "_go_sdk_tools": attr.label(
            default = Label("@go_sdk//:tools"),
            allow_files = True,
            cfg = "host",
        ),
        "_runner": attr.label(
            default = Label("//bazel/rules_gofmt:runner.bash.template"),
            allow_single_file = True,
        ),
    },
    executable = True,
)
