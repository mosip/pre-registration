FROM mosipid/openjdk-21-jre:21.0.4

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

ARG KUBECTL_VERSION=1.22.9

# set working directory for the user
WORKDIR /home/${container_user}

ENV work_dir=/home/${container_user}

# Create the user before COPY --chown so the test jar is not duplicated by a later chown -R.
RUN apt-get -y update \
&& apt-get install -y --no-install-recommends unzip jq curl ca-certificates \
&& rm -rf /var/lib/apt/lists/* \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} \
&& curl -LO "https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl" \
&& chmod +x kubectl \
&& mv kubectl /usr/local/bin/ \
&& chown ${container_user}:${container_user} /etc/ssl/certs/java/cacerts \
&& chmod 644 /etc/ssl/certs/java/cacerts

COPY --chown=${container_user_uid}:${container_user_gid} ./api-test/target $work_dir/
COPY --chown=${container_user_uid}:${container_user_gid} entrypoint.sh $work_dir

RUN chmod +x $work_dir/entrypoint.sh

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

WORKDIR ${work_dir}

EXPOSE 8083

ENV MODULES=
ENV ENV_USER=
ENV ENV_ENDPOINT=
ENV ENV_TESTLEVEL=smokeAndRegression

ENTRYPOINT ["./entrypoint.sh"]
