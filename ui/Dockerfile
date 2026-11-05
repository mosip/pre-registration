FROM nginx

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

ENV base_path=/usr/share/nginx/html

ENV i18n_path=${base_path}/assets/i18n

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG preregistration_i18n_bundle_url_arg=http://artifactory-service/artifactory/libs-release-local/i18n/pre-registration-i18n-bundle.zip

# environment variable to pass artifactory url, at docker runtime
ENV preregistration_i18n_bundle_url_env=${preregistration_i18n_bundle_url_arg}

# wget + unzip are used at runtime for the i18n bundle. Create the user before COPY --chown.
RUN apt-get -y update \
&& apt-get install -y --no-install-recommends unzip wget \
&& rm -rf /var/lib/apt/lists/* \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
&& mkdir -p /var/run/nginx /var/tmp/nginx ${i18n_path} \
&& chown -R ${container_user}:${container_user} /usr/share/nginx /var/run/nginx /var/tmp/nginx

# set working directory for the user
WORKDIR /home/${container_user}

COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/
COPY --chown=${container_user_uid}:${container_user_gid} dist ${base_path}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

EXPOSE 8080

CMD wget -q --show-progress "${preregistration_i18n_bundle_url_env}" -O "${i18n_path}"/pre-registration-i18n-bundle.zip; \
   cd ${i18n_path} ; \
   unzip -o pre-registration-i18n-bundle.zip ; \
   nginx ; \
   sleep infinity
