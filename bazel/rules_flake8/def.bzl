load("@bazel_skylib//lib:shell.bzl", "shell")
load("@flake8_deps//:requirements.bzl", "requirement")

def _flake8_impl(ctx):
    # Taken from https://github.com/bazelbuild/buildtools/blob/bf564b4925ab5876a3f64d8b90fab7f769013d42/buildifier/def.bzl#L15
    exclude_patterns_str = ""
    if ctx.attr.exclude_patterns:
        exclude_patterns = ["-not -path %s" % shell.quote(pattern) for pattern in ctx.attr.exclude_patterns]
        exclude_patterns_str = " ".join(exclude_patterns) + " -and"

    out_file = ctx.actions.declare_file(ctx.label.name + ".bash")
    substitutions = {
        "@@FLAKE8_SHORT_PATH@@": shell.quote(ctx.executable._flake8.short_path),
        "@@EXCLUDE_PATTERNS@@": exclude_patterns_str,
    }

    ctx.actions.expand_template(
        template = ctx.file._runner,
        output = out_file,
        substitutions = substitutions,
        is_executable = True,
    )
    runfiles = ctx.runfiles(
        files = [ctx.executable._flake8],
        transitive_files = ctx.attr._flake8[DefaultInfo].default_runfiles.files,
    )

    return [DefaultInfo(
        runfiles = runfiles,
        executable = out_file,
    )]

flake8 = rule(
    implementation = _flake8_impl,
    attrs = {
        "exclude_patterns": attr.string_list(
            allow_empty = True,
            doc = "A list of glob patterns passed to the find command.",
            default = [
                ".*.git/*",
                ".*.project/*",
                ".*idea/*",
            ],
        ),
        "_flake8": attr.label(
            default = Label("//bazel/rules_flake8:flake8"),
            executable = True,
            cfg = "host",
        ),
        "_runner": attr.label(
            default = Label("//bazel/rules_flake8:runner.bash.template"),
            allow_single_file = True,
        ),
    },
    executable = True,
)
