{
    inputs.nixpkgs.url = github:NixOS/nixpkgs/nixpkgs-unstable;

    outputs = { self, nixpkgs }:
        let pkgs = nixpkgs.legacyPackages.x86_64-linux;
        in {
            devShell.x86_64-linux = {
                server = pkgs.mkShell {
                    buildInputs = [
                        pkgs.jdk17
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
                    ];
                };
            };
        };
}
