# Microsercies with Kubernetes, Quarkus and Microprofile

## Table of Contents
- [Environment setup](#environment-setup)
- [Create Microservice with Quarkus](#create-microservice-with-quarkus)
- [Running PostgreSQL in Kubernetes](#deploy-and-create-postgresql-in-kubernetes)
- [Connect Quarkus service to PostgreSQL](#connect-quarkus-service-to-postgresql)
- [MicroProfile OpenAPI Specification](#microprofile-openapi-specification)
- [MicroProfile Metrics](#microprofile-metrics)
- [View Metrics with Prometheus and Grafana](#view-metrics-with-prometheus-and-grafana)
- [Centralized Logging with EFK](#centralized-logging)
- [Securing Service with Keycloak](#securing-microservice-with-keycloan-as-an-identity-provider)
- [Add OpenID Connect to Quarkus service](#add-openid-connect-to-quarkus-service)

# Video on Youtbe.com
[![Video](https://img.youtube.com/vi/JHI97Pe8vQo/maxresdefault.jpg)](https://youtu.be/JHI97Pe8vQo)

## Environment setup
```java
brew install hyperkit
```
![img.01]

### Install Docker-CLI
```java
brew install docker
```
`NOTE: Do not run brew install --cask docker. This will install Docker Desktop and we will be back to where we started!`
![img.02]

### Install kubectl
```java
brew install kubectl
```
![img.03]
### Install mikikube and set cpu and memory limit
```java
brew install minikube
```
![img.04]

### Start kubernetes cluster
```java
minikube start --driver=hyperkit --container-runtime=docker
```
![img.05]
### Set environment variable
```java
minikube -p minikube docker-env | source
```

### Install docker-compose
```java
brew install docker-compose
```
![img.06]

### Enable usefull minikube addons
```java
minikube addons enable metrics-server

minikube addons enable ingress

minikube addons enable ingress-dns

minikube addons enable metallb

minikube addons configure metallb
```
![img.07]
![img.08]

### Install Docker credential helper
```java
brew install docker-credential-helper
```
![img.09]

# Create Microservice with Quarkus
Run this maven command to creat project scaffold
```java
mvn io.quarkus:quarkus-maven-plugin:2.8.1.Final:create -DprojectGroupId=prajumsook -DprojectArtifactId=country-service -DclassName="org.wj.prajumsook.countries.CountryResource" -Dpath="/countries"
```
For detail please checkout the video.

# Deploy and create PostgreSQL in Kubernetes
Create database instance script
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
spec:
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:12
          imagePullPolicy: Always
          ports:
            - containerPort: 5432
              protocol: TCP
          env:
            - name: POSTGRES_DB
              value: quarkus_db
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: username
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: postgres
  name: postgres
spec:
  ports:
    - name: http
      port: 5432
      protocol: TCP
  selector:
    app: postgres
  type: NodePort
```
Create database secret
```java
kubectl create secret generic db-credentials --from-literal=username=quarkus_user --from-literal=password=quarkus_pass
```
![img.26]

Start PostgreSQL instance
```java
kubectl apply -f postgresql_db.yaml
```
![img.27]
Now you should see that PostgreSQL running on port `32728` (In this tutorial but you should have a differns on your env.)
```java
http://192.168.64.2:32728
```
# Connect Quarkus service to PostgreSQL
Add hibernate-orm and PostgreSQL extension to the project
```java
mvn quarkus:add-extension -Dextensions="hibernate-orm,jdbc-postgresql"
```
![img.28]

`Please check out video for complete changes in the service.`

# MicroProfile OpenAPI Specification
Add OpenAPI extension
```java
mvn quarkus:add-extension -Dextensions="quarkus-smallrye-openapi"
```
`Please watch video for complete changes.`

# MicroProfile Metrics
Add extension
```java
mvn quarkus:add-extension -Dextensions="quarkus-smallrye-metrics"
```
`Please watch video for complete implementation.`

# View Metrics with Prometheus and Grafana
## Deploy, install and config Prometheus and Grafana
Code using in this secstion is from:
```java
https://github.com/prometheus-operator/kube-prometheus
``` 
Please clone the latest from the repo
```java
git clone https://github.com/prometheus-operator/kube-prometheus.git
```

Creates the Kubernetes CRDs
```java
kubectl create -f manifests/setup
```
And then
```java
kubectl create -f manifests
```
Create servicemonitor.yaml file
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: country-service
  namespace: default
  labels:
    app.kubernetes.io/name: country-service
spec:
  namespaceSelector:
    matchNames:
      - default
  selector:
    matchLabels:
      app.kubernetes.io/name: country-service
  endpoints:
  - port: http
    interval: 3s
    path: /q/metrics
```

And to get service monitor service run command
```java
kubectl get servicemonitors --all-namespaces
```
Access grafana dashboard
```java
kubectl port-forward -n monitoring service/grafana 3000:3000
```
And on browser navigate to `http://lcalhost:3000`

Login with user `admin` and pass `admin`

`Please watch video for complete implementation`

# Centralized Logging
## Setup Elasticsearch-Fluentd-Kibana (EFK) in Kubernetes
### Deploy and install Elasticsearch
Create yaml file as follow
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: elasticsearch
spec:
  selector:
    matchLabels:
      component: elasticsearch
  template:
    metadata:
      labels:
        component: elasticsearch
    spec:
      containers:
      - name: elasticsearch
        image: docker.elastic.co/elasticsearch/elasticsearch:7.17.3
        env:
        - name: discovery.type
          value: single-node
        ports:
        - containerPort: 9200
          name: http
          protocol: TCP
        resources:
          limits:
            cpu: 500m
            memory: 4Gi
          requests:
            cpu: 500m
            memory: 4Gi

---

apiVersion: v1
kind: Service
metadata:
  name: elasticsearch
  labels:
    service: elasticsearch
spec:
  type: NodePort
  selector:
    component: elasticsearch
  ports:
  - port: 9200
    targetPort: 9200
```
Create deployment and service
```java
kubectl create -f elasticsearch.yaml
```
### Deploy and install Kibana
Create yaml file as follow
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kibana
spec:
  selector:
    matchLabels:
      run: kibana
  template:
    metadata:
      labels:
        run: kibana
    spec:
      containers:
      - name: kibana
        image: docker.elastic.co/kibana/kibana:7.17.3
        env:
        - name: ELASTICSEARCH_URL
          value: http://<MINIKUBE IP>:<EXPOSED ELASTIC PORT>
        - name: XPACK_SECURITY_ENABLED
          value: "true"
        ports:
        - containerPort: 5601
          name: http
          protocol: TCP

---

apiVersion: v1
kind: Service
metadata:
  name: kibana
  labels:
    service: kibana
spec:
  type: NodePort
  selector:
    run: kibana
  ports:
  - port: 5601
    targetPort: 5601

```
You can find `MINIKUBE IP` with this command
```cmd
minikube ip
```
And
```cmd
kubectl get services
```
You should be able to find `elasticsearch port`

Create deployment and service
```cmd
kubectl create -f kibana.yaml
```
### Config Role-Based Access Controll for Fluentd
Create Fluentd RBAC so that Fluentd can access to logs components
yaml file as follow
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: fluentd
  namespace: kube-system

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: fluentd
  namespace: kube-system
rules:
- apiGroups:
  - ""
  resources:
  - pods
  - namespaces
  verbs:
  - get
  - list
  - watch

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: fluentd
roleRef:
  kind: ClusterRole
  name: fluentd
  apiGroup: rbac.authorization.k8s.io
subjects:
- kind: ServiceAccount
  name: fluentd
  namespace: kube-system
``` 
And run the create
```cmd
kubectl create -f fluentd-rbac.yaml
```
### DaemonSet
Create yaml file as follow
```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluentd
  namespace: kube-system
  labels:
    k8s-app: fluentd-logging
spec:
  selector:
    matchLabels:
      name: fluentd
  template:
    metadata:
      labels:
        name: fluentd
    spec:
      serviceAccount: fluentd
      serviceAccountName: fluentd
      tolerations:
      - key: node-role.kubernetes.io/master
        effect: NoSchedule
      containers:
      - name: fluentd
        image: fluent/fluentd-kubernetes-daemonset:v1.3-debian-elasticsearch
        env:
          - name:  FLUENT_ELASTICSEARCH_HOST
            value: "<MINIKUBE IP>"
          - name:  FLUENT_ELASTICSEARCH_PORT
            value: "<ELASTICSEARCH EXPOSE PORT>"
          - name: FLUENT_ELASTICSEARCH_SCHEME
            value: "http"
          - name: FLUENT_UID
            value: "0"
        resources:
          limits:
            memory: 200Mi
          requests:
            cpu: 100m
            memory: 200Mi
        volumeMounts:
        - name: varlog
          mountPath: /var/log
        - name: varlibdockercontainers
          mountPath: /var/lib/docker/containers
          readOnly: true
      terminationGracePeriodSeconds: 30
      volumes:
      - name: varlog
        hostPath:
          path: /var/log
      - name: varlibdockercontainers
        hostPath:
          path: /var/lib/docker/containers

```
You need to change `MINIKUBE IP` and `ELASTICSEARCH EXPOSE PORT`
Then create the daemonset
```cmd
kubectl create -f fluent-daemonset.yaml
```

`Please watch the video for complete implementation`

# Securing Microservice with Keycloan as an Identity provider
## Config and running Keycloak in Kubernetes
Create a keycloak.yaml file as follow
```yaml
apiVersion: v1
kind: Service
metadata:
  name: keycloak
  labels:
    app: keycloak
spec:
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  selector:
    app: keycloak
  type: LoadBalancer
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: keycloak
  labels:
    app: keycloak
spec:
  replicas: 1
  selector:
    matchLabels:
      app: keycloak
  template:
    metadata:
      labels:
        app: keycloak
    spec:
      containers:
      - name: keycloak
        image: quay.io/keycloak/keycloak:18.0.0
        args: ["start-dev"]
        env:
        - name: KEYCLOAK_ADMIN
          value: "admin"
        - name: KEYCLOAK_ADMIN_PASSWORD
          value: "admin"
        - name: KC_PROXY
          value: "edge"
        ports:
        - name: http
          containerPort: 8080
        readinessProbe:
          httpGet:
            path: /realms/master
            port: 8080
```
Then run this command
```java
kubectl create -f keycloak.yaml
```
![img.10]

This will start keycloak on Kubernetes and it will also create and initial admin user with username `admin` and password `admin`

### Next create an Ingress for Keycloak
keycloak-ingress.yaml
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: keycloak
spec:
  tls:
    - hosts:
      - KEYCLOAK_HOST
  rules:
  - host: KEYCLOAK_HOST
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: keycloak
            port:
              number: 8080
```
Replace `KEYCLOAK_HOST` with `keycloak.{minikube ip address}.nip.io`

To find out the `minikube ip address` you can run this command
```java
minikube ip
```

Then run this command to create ingress for Keycloak
```java
kubectl create -f keycloak-ingress.yaml
```
![img.11]

Next check out minikube service list

Run command
```java
minikube service list
```
![img.12]

You should see keycloak url, open your browser and enter keycloal url
![img.13]

Login with username `admin` and password `admin`
![img.14]

You should see the Keycloak dashboard
![img.15]

## Config Keycloak as an Identity provider
Create new Realm name `quarkus`
![img.16]
![img.17]
Create a new User `quarkus-user`
![img.18]

Create new client `quarkus-client` with Client Protocol `openid-connect`, Access Type `confidential`, Standard Flow `on` and Direct Access Grants Enabled `on`

Create new client `quarkus-client`
![img.19]

Config client
![img.20]
![img.21]
![img.22]

Set Valid Redirect URIs to somthing like `http://localhost:99999/callback` for now. 

Save your settings and click on `Credentials` tab, make note of your Client name and Clent secret.
![img.23]

Open up Postman and enter this url:
```java
http://192.168.64.2:31803/realms/quarkus/.well-known/openid-configuration
```
![img.24]

We are interested in `token_endpoint`, copy that and enter to new Postman request as `POST`.

Enter post body as `x-www-form-urlencoded`:
```java
client_id = quarkus-client
client_secret = <copy from Client page>
username = quarkus-user
password = password
grant_type = password
```

You should get the response with `access_token`, `refresh_token` and more. This access_token you will need to identify you service calls.
![img.25]

# Add OpenID Connect to Quarkus service
Add quarkus extension
```java
mvn quarkus:add-extension -Dextensions="quarkus-oidc"
```
`Please watch the video for complete implementation`

[img.00]: country-service/assets/img-00.png
[img.01]: country-service/assets/img-01.png
[img.02]: country-service/assets/img-02.png
[img.03]: country-service/assets/img-03.png
[img.04]: country-service/assets/img-04.png
[img.05]: country-service/assets/img-05.png
[img.06]: country-service/assets/img-06.png
[img.07]: country-service/assets/img-07.png
[img.08]: country-service/assets/img-08.png
[img.09]: country-service/assets/img-09.png
[img.10]: country-service/assets/img-10.png
[img.11]: country-service/assets/img-11.png
[img.12]: country-service/assets/img-12.png
[img.13]: country-service/assets/img-13.png
[img.14]: country-service/assets/img-14.png
[img.15]: country-service/assets/img-15.png
[img.16]: country-service/assets/img-16.png
[img.17]: country-service/assets/img-17.png
[img.18]: country-service/assets/img-18.png
[img.19]: country-service/assets/img-19.png
[img.20]: country-service/assets/img-20.png
[img.21]: country-service/assets/img-21.png
[img.22]: country-service/assets/img-22.png
[img.23]: country-service/assets/img-23.png
[img.24]: country-service/assets/img-24.png
[img.25]: country-service/assets/img-25.png
[img.26]: country-service/assets/img-26.png
[img.27]: country-service/assets/img-27.png
[img.28]: country-service/assets/img-28.png
