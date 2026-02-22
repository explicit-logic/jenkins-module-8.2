# Module 8 - Build Automation & CI/CD with Jenkins

This repository contains a demo project created as part of my **DevOps studies** in the **TechWorld with Nana – DevOps Bootcamp**.

https://www.techworld-with-nana.com/devops-bootcamp

***Demo Project:*** Create a CI Pipeline with Jenkinsfile (Freestyle, Pipeline, Multibranch Pipeline)

***Technologies used:*** Jenkins, Docker, Linux, Git, Java, Maven

***Project Description:*** 

CI Pipeline for a Java Maven application to build and push to the repository

- Install Build Tools (Maven, Node) in Jenkins
- Make Docker available on Jenkins server
- Create Jenkins credentials for a git repository
- Create different Jenkins job types (Freestyle, Pipeline, Multibranch pipeline) for the Java Maven project with Jenkinsfile to:
  - a. Connect to the application’s git repository
  - b. Build Jar
  - c. Build Docker Image
  - d. Push to private DockerHub repository

---

### Prerequisites

Before starting, complete all steps to install Jenkins on DigitalOcean:

- [jenkins-module-8.1](https://github.com/explicit-logic/jenkins-module-8.1)

---

### Install Build Tools (Maven, Node) in Jenkins

#### 1. Configure Maven plugin

Navigate to **Manage Jenkins** > **Tools** > **Maven installations**

Click **Add Maven** and fill in:

| Field | Value |
|-------|-------|
| Name  | `maven-3.9` |

Click **Save**.

![Configure Maven](./images/configure-maven.png)

#### 2. Install Node.js and npm inside the Jenkins container

Enter the Jenkins Docker container as the `root` user:

```sh
docker exec -u 0 -it <container_id> bash
```

Update packages and install `curl`:

```sh
apt update
apt install -y curl
```

Download and run the NodeSource setup script, then install Node.js:

```sh
curl -sL https://deb.nodesource.com/setup_20.x -o nodesource_setup.sh
bash nodesource_setup.sh
apt install -y nodejs
```

Verify the installation:

```sh
node -v   # should print v20.x.x
npm -v    # should print a version number
```

#### 3. Install the `Pipeline Stage View` plugin

- Navigate to **Manage Jenkins** > **Plugins** > **Available plugins**
- Search for `Pipeline Stage View`, select it, and click **Install**
- Check **Restart Jenkins when installation is complete and no jobs are running**

![Stage View plugin](./images/stage-view.png)

If Jenkins does not restart automatically, start it manually:

```sh
docker ps -a
docker start <container_id>
```

- Navigate to **Manage Jenkins** > **Plugins** > **Installed plugins** to confirm the plugin is active.

---

### Make Docker Available on the Jenkins Server

> **Why?** Jenkins needs access to the host Docker daemon so it can build and push images as part of the pipeline.

#### 1. Stop the existing Jenkins container

```sh
docker ps
docker stop <container_id>
```

#### 2. Start a new Jenkins container with Docker socket mounted

```sh
docker run -p 8080:8080 -p 5000:5000 -d \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

#### 3. Enter the container as `root` and install Docker

```sh
docker ps
docker exec -it -u 0 <container_id> bash
```

Run the official Docker install script:

```sh
curl https://get.docker.com/ > dockerinstall && chmod 755 dockerinstall && ./dockerinstall
```

#### 4. Grant the Jenkins user access to the Docker socket

```sh
chmod 666 /var/run/docker.sock
```

#### 5. Re-enter the container as the `jenkins` user and verify

```sh
exit
docker exec -it <container_id> bash
docker pull redis   # should succeed if Docker access is working
```

---

### Create Jenkins Credentials for GitHub

1. Navigate to **Manage Jenkins** > **Credentials** > **System** > **Global credentials**
2. Click **Add Credentials** and fill in:

| Field | Value |
|-------|-------|
| Kind | `Username with password` |
| Username | `<Your GitHub username>` |
| Password | `<Your GitHub password>` |
| Treat username as secret | `Yes` |
| ID | `github` |

3. Click **Create**.

---

### Create a Private Docker Hub Repository

1. Log in to [Docker Hub](https://hub.docker.com/repositories/)
2. Click **Create Repository** and fill in:

| Field | Value |
|-------|-------|
| Name | `app` |
| Visibility | `Private` |

3. Click **Create**.

---

### Create Jenkins Credentials for Docker Hub

1. Navigate to **Manage Jenkins** > **Credentials** > **System** > **Global credentials**
2. Click **Add Credentials** and fill in:

| Field | Value |
|-------|-------|
| Kind | `Username with password` |
| Username | `<Your Docker Hub username>` |
| Password | `<Your Docker Hub password>` |
| Treat username as secret | `Yes` |
| ID | `docker` |

3. Click **Create**.

---

### Create a `Freestyle` Jenkins Job

#### 1. Create the job

- From the dashboard, click **New Item** (or **Create a job**)
- Name: `freestyle`
- Type: **Freestyle project**
- Click **OK**

#### 2. Configure Source Code Management

Under **Source Code Management**, select **Git** and fill in:

| Field | Value |
|-------|-------|
| Repository URL | `https://github.com/explicit-logic/jenkins-module-8.2` |
| Credentials | `github` |
| Branches to build | `*/freestyle` |

#### 3. Bind Docker credentials as environment variables

Under **Build Environment**, check **Use secret text(s) or file(s)**, then click **Add** > **Username and Password (separated)**:

| Field | Value |
|-------|-------|
| Username Variable | `USERNAME` |
| Password Variable | `PASSWORD` |
| Credentials | `docker` |

#### 4. Add Maven test step

Click **Add build step** > **Invoke top-level Maven targets**:

| Field | Value |
|-------|-------|
| Maven Version | `maven-3.9` |
| Goals | `test` |
| POM | `app/pom.xml` |

#### 5. Add Maven package step

Click **Add build step** > **Invoke top-level Maven targets** again:

| Field | Value |
|-------|-------|
| Maven Version | `maven-3.9` |
| Goals | `package` |
| POM | `app/pom.xml` |

#### 6. Add Docker build & push step

Click **Add build step** > **Execute Shell** and enter:

```sh
cd app
docker build -t <docker_username>/app:freestyle .
echo $PASSWORD | docker login -u $USERNAME --password-stdin
docker push <docker_username>/app:freestyle
```

> Replace `<docker_username>` with your Docker Hub username.

![Freestyle job configuration](./images/freestyle-config.png)

#### 7. Run the job

Click **Build Now** and monitor the console output.

![Freestyle Demo](./images/jenkins-freestyle.gif)

---

### Create a `Pipeline` Jenkins Job

#### 1. Create the job

- From the dashboard, click **New Item**
- Name: `pipeline`
- Type: **Pipeline**
- Click **OK**

#### 2. Configure the Pipeline

Scroll to the **Pipeline** section and fill in:

| Field | Value |
|-------|-------|
| Definition | `Pipeline script from SCM` |
| SCM | `Git` |
| Repository URL | `https://github.com/explicit-logic/jenkins-module-8.2` |
| Credentials | `github` |
| Branches to build | `*/pipeline` |

Jenkins will look for a `Jenkinsfile` at the root of the branch automatically.

#### 3. Run the job

Click **Build with Parameters** and fill in:

| Parameter | Value |
|-----------|-------|
| `DOCKER_REPO` | `<docker_username>/app:pipeline` |

![Pipeline parameters](./images/pipeline-params.png)

![Jenkins Pipeline Demo](./images/jenkins-pipeline.gif)

---

### Create a `Multibranch Pipeline` Jenkins Job

#### 1. Create the job

- From the dashboard, click **New Item**
- Name: `multibranch`
- Type: **Multibranch Pipeline**
- Click **OK**

#### 2. Configure Branch Sources

Under **Branch Sources**, click **Add source** > **Git** and fill in:

| Field | Value |
|-------|-------|
| Project Repository | `https://github.com/explicit-logic/jenkins-module-8.2` |
| Credentials | `github` |

Click **Save**. Jenkins will automatically scan the repository and create jobs for branches that contain a `Jenkinsfile`.

#### 3. Run the job

Click **Build with Parameters** and fill in:

| Parameter | Value |
|-----------|-------|
| `DOCKER_REPO` | `<docker_username>/app` |

> **Notes:**
> - The Docker image tag is derived from the branch name (e.g., `main`, `pipeline`).
> - The test phase runs **only** on the `main` branch.

![Multibranch Pipeline Demo](./images/jenkins-multibranch.gif)
