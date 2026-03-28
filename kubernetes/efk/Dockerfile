FROM fluent/fluentd-kubernetes-daemonset:v1-debian-elasticsearch8-1

RUN gem install fluent-plugin-detect-exceptions -v 0.0.12 \
 && gem sources --clear-all \
 && rm -rf /var/lib/apt/lists/* \
           /home/fluent/.gem/ruby/2.3.0/cache/*.gem
