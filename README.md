 🛠️ Step 1: AWS EKS Cluster Provisioning & Storage CSI Drivers
Before deploying Kafka, you need a highly resilient Kubernetes infrastructure. We use eksctl to automatically spin up nodes in multiple availability zones and tie IAM permissions for dynamic disk provisioning.

1.1 Create the EKS Cluster Topology Setup (cluster.yaml)
Create a file named cluster.yaml to layout your multi-AZ managed node group configuration:


Execute this CLI command to launch the infrastructure stack:

Bash
eksctl create cluster -f cluster.yaml
1.2 Enable the AWS EBS CSI Driver
Kubernetes cannot talk directly to AWS storage blocks out of the box. Install the cloud native CSI storage broker driver as an active EKS cluster add-on:

Bash
eksctl create addon --cluster production-kafka-cluster --name aws-ebs-csi-driver --use-default-version --force
📦 Step 2: Infrastructure Layer & Apache Kafka Declarations
Create a subfolder named k8s/infrastructure/ and declare the cloud storage rules, zookeeper configuration, and Kafka StatefulSet topology.

2.1 StorageClass YAML (k8s/infrastructure/01-storageclass.yaml)
This specifies that the EKS cluster should provision ultra-low-latency gp3 AWS block storage dynamically, waiting until the application pod maps onto an availability zone.


2.2 Zookeeper Deployment YAML (k8s/infrastructure/02-zookeeper.yaml)
Kafka uses Zookeeper for consensus. Set up a lightweight cluster coordinate node:

2.3 Kafka StatefulSet & Headless Service (k8s/infrastructure/03-kafka.yaml)
By using a StatefulSet along with volumeClaimTemplates, Kubernetes guarantees that whenever a Kafka broker pod (like kafka-0) initializes or restarts, it binds to the exact same AWS EBS disk storage.


Deploy the persistent cluster infrastructure layer by running:

Bash
kubectl apply -f k8s/infrastructure/
☕ Step 3: Spring Boot Java Application Layer Logic
Both decoupled microservices rely upon Java 21 features and standard JSON serialization frameworks to pass data models smoothly.

3.1 Shared Records Data Model
Both services use this records domain definition to mirror transactional event schema boundaries:

Java
package com.example.kafka.model;

public record OrderEvent(String orderId, String item, double price, String status) {}
3.2 Producer Microservice Construction
producer-app/pom.xml dependencies

producer-app/src/main/resources/application.yml

REST Controller Layer (OrderProducerController.java)

3.3 Consumer Microservice Construction
consumer-app/src/main/resources/application.yml

Kafka Message Consumer Stream Handler (OrderConsumerService.java)

🐳 Step 4: Multi-Stage Production Grade Dockerization
Place this optimized multi-stage file within both application folders (./producer-app/Dockerfile and ./consumer-app/Dockerfile). It isolates building tools from runtime containers, creating lean images with small security footprints.

Dockerfile

🚀 Step 5: Continuous Integration (CI) Workflow Pipeline
Save this file under .github/workflows/ci-pipeline.yml in your code repo. This runs automatic Maven compilations, builds localized container artifacts, fires tags over to your public Docker Hub repository, and patches deployment manifests dynamically.

k8s/producer-deployment.yaml
      
🌐 Step 6: Application Manifest Spec Deployments
Place these declarative files in your git structure inside k8s/apps/.

6.1 Producer Deployment (k8s/producer-deployment.yaml)

6.2 Consumer Deployment (k8s/apps/consumer-deployment.yaml)

🐙 Step 7: Continuous Delivery via Argo CD (GitOps)
Argo CD handles delivery by tracking the desired configuration state defined in Git and modifying your live EKS cluster resources to match it.

7.1 Setup Argo CD Engine Components
Bash
kubectl create namespace argocd
kubectl apply -n argocd -f [https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml](https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml)
7.2 Pull Access Credentials
Expose the server dashboard on a local browser loop:

Bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
Decode the initial cluster generated root administrator login password:

Bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
7.3 Declare the Parent GitOps Application Target (argo-app.yaml)
Run kubectl apply -f argo-app.yaml to trigger automated reconciliation across the repository tree.


🧪 Step 8: Verifying End-to-End Pipeline Functions
1. Confirm Cluster Readiness
Ensure Kafka pods are healthy and running:

Bash
kubectl get pods -n kube-system | grep kafka
2. Stream Live Test Transactions
Forward traffic ports locally to query the producer app:

Bash
kubectl port-forward svc/kafka-producer-service 8080:8080
Trigger a mock JSON HTTP POST transaction object payload:

Bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"TX-10042","item":"MacBook Pro M3","price":2399.00,"status":"PENDING"}'
3. Monitor Real-time Consumer Delivery Logs
Check the logs of the consumer application to ensure it processes the messages successfully from the persistent AWS storage tier:

Bash
kubectl logs -l app=kafka-consumer --tail=50
Expected execution log receipt output:

Plaintext
Successfully Consumed Event Order Id -> TX-10042, Item -> MacBook Pro M3, Status -> PENDING
"""

with open("README.md", "w") as f:
f.write(readme_content.strip())
print("File written successfully.")

Your complete installation and configuration instructions have been compiled into a single, comprehensive `README.md` file. This file contains the architecture, configuration steps, Spring Boot code snippets, Dockerfiles, GitHub Actions workflows, and Argo CD manifests.

[file-tag: code-generated-file-0-1782447796886006225]

### 📁 Inside this `README.md` File:
* **EKS Cluster & Storage Topology Setup:** Commands and definitions (`cluster.yaml`) to build a multi-AZ cluster with dynamic `gp3` storage provisioned via the **AWS EBS CSI Driver**.
* **Kafka Multi-Node Architecture:** Complete manifests for Zookeeper and a 3-broker Apache Kafka setup using a **StatefulSet** backed by persistent EBS storage volumes.
* **Spring Boot 3.x Applications (Java 21):** Production-grade source code for both the **Producer** (REST Controller mapping inputs to JSON payloads) and the **Consumer** (listening dynamically to events).
* **Multi-Stage Dockerfiles:** Performance-optimized build scripts that isolate build environments from small, fast production runtimes.
* **GitHub Actions Workflow Execution (`ci-pipeline.yml`):** Fully automated CI pipeline that tests code, publishes built images to Docker Hub, and pushes tag changes back to Git.
* **Argo CD Application Manifest (`argo-app.yaml`):** Implements a declarative GitOpps engine to automatically pull, apply, and repair deployments in your AWS EKS cluster.
