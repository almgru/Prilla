{
    inputs.nixpkgs.url = github:NixOS/nixpkgs/nixpkgs-unstable;

    outputs = { self, nixpkgs }:
        let pkgs = nixpkgs.legacyPackages.x86_64-linux;
        in {
            devShell.x86_64-linux = {
                server = pkgs.mkShell {
                    buildInputs = [
                        pkgs.jdk20
                        pkgs.jdt-language-server
                    ];
                };

                client-web = pkgs.mkShell {
                    buildInputs = [
                        pkgs.nodejs_20
                    ];
                };

                client-android = pkgs.mkShell {
                    buildInputs = [
                        pkgs.jdk17
                        pkgs.kotlin-language-server
                    ];
                    shellHook = ''
                        GIT_ROOT="$(git rev-parse --show-toplevel)"
                        export ANDROID_HOME="$GIT_ROOT"/client/android/.android
                        export ANDROID_SDK_ROOT="$ANDROID_HOME"
                        export GRADLE_OPTS="-Dorg.gradle.jvmargs='-Xms64m -Xmx512m'"
                        export PATH="$PATH":$"$ANDROID_HOME"/cmdline-tools/latest/bin
                    '';
                };
            };
        };
}
