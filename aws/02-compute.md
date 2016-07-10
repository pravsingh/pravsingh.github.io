
## Compute
### AWS Training Module 2

---

## Agenda

- Virtual machines
- Identity and Access management
- Virtual networks
- Auto Scaling and Load-balancing

--

## Prerequisites

- Browser and Internet access
- SSH client (e.g. [Putty](http://www.chiark.greenend.org.uk/~sgtatham/putty/download.html) on Windows)

---

# Elastic Compute Cloud

--

## [Elastic Compute Cloud (EC2)](http://aws.amazon.com/ec2/)

- One of the core services of AWS
- Virtual machines (or [*instances*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Instances.html)) as a service
- Dozens of [*instance types*](http://aws.amazon.com/ec2/instance-types/) that vary in performance and cost
- Instance is created from an [*Amazon Machine Image (AMI)*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html), which in turn can be created again from instances

--

![AWS Region map](/images/aws_map_regions.png)

[Regions](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html) and CDN [Edge Locations](http://aws.amazon.com/about-aws/global-infrastructure/)

Notes: Regions: Frankfurt, Ireland, US East (N. Virginia), US West (N. California), US West (Oregon), South America (Sao Paulo), Tokyo, Singapore, Sydney. Special regions are **GovCloud** and **Beijing**.

--

![AWS EU Region map](/images/aws_map_regions_eu.png)

[Regions and Availability Zones (AZ)](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html)

Notes: We will only use Ireland (eu-west-1) region in this workshop. See also [A Rare Peek Into The Massive Scale of AWS](http://www.enterprisetech.com/2014/11/14/rare-peek-massive-scale-aws/).

--

## Networking in AWS

- Regions and availability zones
- [*Security groups*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html) provide port-level firewalls to instances
- More detailed IP subnetting with [Virtual Private Cloud (VPC)](http://aws.amazon.com/vpc/)

--

## Exercise: Launch an EC2 instance

1. Log-in to [gofore-crew.signin.aws.amazon.com/console](https://gofore-crew.signin.aws.amazon.com/console)
2. Switch to **Ireland** region and go to EC2 dashboard
3. Launch a new EC2 instance according instructor guidance
  - In *"Configure Instance Details"*, pass a [*User Data*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html) script under *Advanced*
  - In *"Configure Security Group"*, use a recognizable, unique name

<pre><code data-trim="" class="shell">
#!/bin/sh
# When passed as User Data, this script will be run on boot
touch /new_empty_file_we_created.txt
echo "It works!" > /it_works.txt
</code></pre>

--

## Exercise: SSH into the instance

SSH into the instance (find the IP address in the EC2 console)

    # Windows Putty users must convert key to .ppk (see notes)
    ssh -i your_ssh_key.pem ubuntu@instance_ip_address

View instance metadata

    curl http://169.254.169.254/latest/meta-data/

View your [*User Data*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html) and find the changes your script made

    curl http://169.254.169.254/latest/user-data/
    ls -la /

Notes: You will have to reduce keyfile permissions `chmod og-xrw mykeyfile.pem`. If you are on Windows and use Putty, you will have to convert the .pem key to .ppk key using [puttygen](http://www.chiark.greenend.org.uk/~sgtatham/putty/download.html) (Conversions -> Import key -> *.pem file -> Save private key. Now you can use your *.ppk key with Putty: Connection -> SSH -> Auth -> Private key file)

--

## Exercise: Security groups

Setup a web server that hosts the id of the instance

    mkdir ~/webserver && cd ~/webserver
    curl http://169.254.169.254/latest/meta-data/instance-id > index.html
    python -m SimpleHTTPServer

Configure the security group of your instance to allow inbound requests to your web server from **anywhere**. Check that you can access the page with your browser.

--

## Exercise: Security groups

Delete the previous rule. Ask a neighbor for the name of their security group, and allow requests to your server from your **neighbor's security group**.

Have your neighbor access your web server from his/her instance.

    # Private IP address of the web server (this should work)
    curl 172.31.???.???:8000
    # Public IP address of the web server (how about this one?)
    curl 52.??.???.???:8000

--

Speaking of IP addresses, there is also [Elastic IP Address](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html). Later on, we will see use cases for this, as well as better alternatives.

Also, notice the monitoring metrics. These come from CloudWatch. Later on, we will create alarms based on the metrics.

--

## [Elastic Block Store (EBS)](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AmazonEBS.html)

- Block storage service (virtual hard drives) with speed and encryption options
- Disks (or [*volumes*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EBSVolumes.html)) are attached to EC2 instances
- [*Snapshots*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EBSSnapshots.html) can be taken from volumes
- Alternative to EBS is ephemeral [*instance store*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/InstanceStorage.html)

--

## EC2 cost

- Instances are billed every starting [*instance-hour*](http://aws.amazon.com/ec2/pricing/)
- Purchasing options of [*On-Demand Instances*, *Reserved Instances*, *Spot Instances*](http://aws.amazon.com/ec2/purchasing-options/)
- Storage costs for volumes, snapshots and images
- Traffic costs (more the further the traffic is towards the Internet)

---

# Identity and Access Management

--

## [Identity and Access Management (IAM)](http://aws.amazon.com/iam/)

- Manage AWS user [*credentials*](http://docs.aws.amazon.com/IAM/latest/UserGuide/Using_ManagingLogins.html) for Web console and API access
- Fine-grained access [*policies*](http://docs.aws.amazon.com/IAM/latest/UserGuide/policies.html) to services and resources
- [*Roles*](http://docs.aws.amazon.com/IAM/latest/UserGuide/roles-toplevel.html) allow applications and external services to access resources
- Also [*Multi-Factor Authentication (MFA)*](http://aws.amazon.com/iam/details/mfa/) and [*Security Token Service (STS)*](http://docs.aws.amazon.com/STS/latest/UsingSTS/Welcome.html)

Notes: Always use roles inside instances (do not store credentials there), or [something bad](http://www.browserstack.com/attack-and-downtime-on-9-November) might happen.

--

## Quiz: Users on many levels

Imagine running a content management system, discussion board or blog web application in EC2. How many **different types** of user accounts you might have?

---

# Virtual Private Cloud

--

## [Virtual Private Cloud (VPC)](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Introduction.html)

- Heavy-weight virtual IP networking for EC2 and RDS instances. Integral part of modern AWS, all instances are launched into VPCs (*not true for [EC2-classic](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-vpc.html)*)
- An AWS root account can have many VPCs, each in a specific region
- Each VPC is divided into [*subnets*](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Subnets.html), each bound to an availability zone
- Each instance connects to a subnet with a [*Elastic Network Interface*](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-eni.html)

--

![VPC with Public and Private Subnets](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/images/nat-instance-diagram.png)

[VPC with Public and Private Subnets](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Scenario2.html)

--

## Access Control Lists

- Network [*Access Control List (ACL)*](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_ACLs.html) provide a second layer of security
- See [Comparison of Security Groups and Network ACLs](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Security.html#VPC_Security_Comparison)

--

![VPC with Public and Private Subnets and Hardware VPN Access](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/images/Case3_Diagram.png)

[VPC with Public and Private Subnets and Hardware VPN Access](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Scenario3.html)

--

## Recap

- Instance and Elastic Network Interface
- Region and Availability Zone
- VPC, Subnet, Route Table
- Network ACL, Instance Security Group
- Internet Gateway, Virtual Private Gateway, NAT instance

--

## Quiz: Separating environments

You have *Development* and *Production* environments.

Both of them have *web servers* and *database servers*.

How would you separate them?


---

# [Auto Scaling](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/WhatIsAutoScaling.html)

--

![Static provisioning](/images/provisioning_static.png)

Problem of traditional capacity planning

--

![Elastic provisioning](/images/provisioning_elastic.png)

Provisioning capacity as needed

--

- Changing the instance type is vertical scaling (*scale up, scale down*)
- Adding or removing instances is horizontal scaling (*scale out, scale in*)
- 1 instance 10 hours = 10 instances 1 hour

--

## Auto Scaling instances

- [*Launch Configuration*](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/LaunchConfiguration.html) describes the configuration of the instance. Having a good AMI and bootstrapping is crucial.
- [*Auto Scaling Group*](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/AutoScalingGroup.html) contains instances whose lifecycles are automatically managed by CloudWatch alarms or schedule
- [*Scaling Plan*](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/scaling_typesof.html) refers when scaling happens and what triggers it.

--

## [Scaling Plans](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/scaling_typesof.html)

- Maintain current number of instances
- Manual scaling by user interaction or via API
- Scheduled scaling
- [Dynamic Auto Scaling](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/as-scale-based-on-demand.html). A *scaling policy* describes how the group scales in or out. You should always have policies for both directions. [*Policy cooldowns*](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/Cooldown.html) control the rate in which scaling happens.

--

![Auto Scaling Group Lifecycle](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/images/as-lifecycle-basic-diagram.png)

[Auto Scaling Group Lifecycle](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/AutoScalingGroupLifecycle.html)

--

## [Elastic Load Balancer](http://aws.amazon.com/elasticloadbalancing/)

- Route traffic to an Auto Scaling Group (ASG)
- Runs health checks to instances to decide whether to route traffic to them
- Spread instances over multiple AZs for higher availability
- ELB scales itself. Never use ELB IP address. Pre-warm before flash traffic.

--

![ELB Architecture](http://awsmedia.s3.amazonaws.com/2012-02-24-techdoc-elb-arch.png)

[Best practices in ELB](https://aws.amazon.com/articles/1636185810492479)

--

### Exercise: Elastic Load Balancer

- Short name for your ELB (will show up in URLs)
- Route HTTP traffic to instance port **9001**
- New security group for the ELB. Allow port **80** from anywhere
- HTTP Health Check, ping **:9001/healthcheck**. Lower interval to 15 and Healthy threshold to 3
- Do not add any instances. Disable connection draining
- Add Name tag

Notes: [Auto Scaling](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/GettingStartedTutorial.html)

--

### Exercise: Launch Configuration

- My AMIs -> Latest **standalone ui**. Micro instance
- IAM role **aws-workshop-ui-role**
- Enable CloudWatch detailed monitoring
- New security group for the instances. Allow **22** and **9001** from anywhere (let's talk about this later)

--

### Exercise: Auto Scaling Group

- Start with **1** instance, Launch into **ALL** subnets
- Receive traffic from your ELB
- **EC2-based** Health Check, **20**-second Grace Period
- Enable CloudWatch detailed monitoring
- Use scaling policies to scale between **1** and **3** instances. Create Scaling **Policy**, CloudWatch **Alarm** and SNS **Topic** for both scaling directions:
  - Alarm when Average CPU > 50% for 2 periods of 5 minutes. Add 1 instance and wait 60 seconds.
  - Alarm when Average CPU < 20% for 2 periods of 5 minutes. Remove 1 instance and wait 60 seconds.
- Send notifications to a new (third) SNS **Topic**. Add Name tag

--

### Does it work?

- Confirm the emails in your inbox to receive notifications
- Look at EC2 -> Auto Scaling -> Auto Scaling Groups
- Look at EC2 -> Network & Security -> Load Balancers
- Look at EC2 -> Instances -> Instances
- Look at CloudWatch -> Alarms

On Windows: If the ELB hostname does not seem to resolve, run `ipconfig /flushdns`

--

## Exercise: Monkey time!

![Chaos Monkey](/images/netflix-chaos-monkey.jpg)

Be a [Chaos Monkey](https://github.com/Netflix/SimianArmy/wiki/Chaos-Monkey): terminate an instance from your auto scaling group

---

# Public networking

--

## [Route 53](http://aws.amazon.com/route53/)

- Domain Name System (DNS)
- Manage *DNS records* of *hosted zones*
- Round Robin, Weighted Round Robin and Latency-based routing

--

## [CloudFront](http://aws.amazon.com/cloudfront/)

- Content Delivery Network (CDN)
- Replicate static content from S3 to edge locations
- Also supports dynamic and streaming content

---

# Recap

--

## Recap

- [EC2](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/concepts.html): Region, Availability Zone, Instance, Security group, AMI, [EBS](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AmazonEBS.html) volume, EBS snapshot, Instance store
- [IAM](http://docs.aws.amazon.com/IAM/latest/UserGuide/IAM_Introduction.html): User, User group, Policy, Permission, Role, API access key, Root account
- [VPC](http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/VPC_Introduction.html): VPC, Subnet, Route table, ACL, NAT instance, Internet gateway, Virtual Private gateway
- [Auto Scaling](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/WhatIsAutoScaling.html): Auto Scaling Group, Launch configuration, Scaling policy, [Elastic Load Balancer](http://docs.aws.amazon.com/ElasticLoadBalancing/latest/DeveloperGuide/elastic-load-balancing.html)
