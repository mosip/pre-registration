#!/bin/sh

# Runtime loader.path jars that are not Maven dependencies (country-pluggable SPIs).
# kernel-auth-adapter is a Maven dependency. TransliterationImpl lives in kernel-core.
set -e

echo "Downloading kernel ref-idobject validator into loader.path (not a Maven dependency)"
wget -q --show-progress "${kernel_ref_idobjectvalidator_url}" -O "${loader_path_env}"/kernel-ref-idobjectvalidator.jar
echo "Downloaded kernel ref-idobject validator"

echo "Downloading kernel virusscanner (ClamAV) into loader.path (not a Maven dependency)"
wget -q --show-progress "${virusscanner_url_env}" -O "${loader_path_env}"/kernel-virusscanner-clamav.jar
echo "Downloaded kernel virusscanner"

exec "$@"
