load("@bazel_skylib//lib:paths.bzl", "paths")
load("@bazel_skylib//lib:shell.bzl", "shell")

def _addlicense_impl(ctx):
    out_file = ctx.actions.declare_file(ctx.label.name + ".bash")

    exclude_patterns_str = ""
    if ctx.attr.exclude_patterns:
        exclude_patterns = ["-not -path %s" % shell.quote(pattern) for pattern in ctx.attr.exclude_patterns]
        exclude_patterns_str = " ".join(exclude_patterns)

    substitutions = {
        "@@ADDLICENSE_SHORT_PATH@@": shell.quote(ctx.executable._addlicense.short_path),
        "@@MODE@@": shell.quote(ctx.attr.mode),
        "@@EXCLUDE_PATTERNS@@": exclude_patterns_str,
    }

    ctx.actions.expand_template(
        template = ctx.file._runner,
        output = out_file,
        substitutions = substitutions,
        is_executable = True,
    )
    runfiles = ctx.runfiles(files = [ctx.executable._addlicense])
    return [DefaultInfo(
        runfiles = runfiles,
        executable = out_file,
    )]

addlicense = rule(
    implementation = _addlicense_impl,
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
        "_addlicense": attr.label(
            default = Label("@com_github_google_addlicense//:addlicense"),
            executable = True,
            cfg = "host",
        ),
        "_runner": attr.label(
            default = Label("//bazel/rules_addlicense:runner.bash.template"),
            allow_single_file = True,
        ),
    },
    executable = True,
)
