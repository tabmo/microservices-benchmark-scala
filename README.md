# Install

## Nginx

**m4.large ubuntu (2 cores)**

```
sudo apt-get update
sudo apt-get install nginx -y

sudo su

echo '{"coord":{"lon":24.96,"lat":60.17},"weather":[{"id":500,"main":"Rain","description":"légères pluies","icon":"10d"}],"base":"stations","main":{"temp":14.65,"pressure":996,"humidity":93,"temp_min":13.89,"temp_max":15},"visibility":10000,"wind":{"speed":4.1,"deg":100},"rain":{"1h":0.2},"clouds":{"all":90},"dt":1436382481,"sys":{"type":1,"id":5018,"message":0.0161,"country":"FI","sunrise":1436317820,"sunset":1436384349},"id":650744,"name":"Kruununhaka","cod":200}' >> /usr/share/nginx/html/weather.json

curl localhost/weather.json
curl "http://ec2-52-18-90-210.eu-west-1.compute.amazonaws.com/weather.json"
```

## Benchmark machine

**m4.4xlarge ubuntu (16 cores)**

```
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install git oracle-java8-installer unzip -y
```

### Spray

```
mkdir spray
cd spray
wget https://s3-eu-west-1.amazonaws.com/tabmo-eng/Bench/weather-api-spray-0.1.zip
unzip weather-api-spray-0.1.zip
./weather-api-spray-0.1/bin/weather-api-spray
```

### Play

```
mkdir play
cd play
wget https://s3-eu-west-1.amazonaws.com/tabmo-eng/Bench/weather-api-play-0.0.1.zip
unzip weather-api-play-0.0.1.zip
./weather-api-play-0.0.1/bin/weather-api-play
```

### Finagle

```
mkdir finagle
cd finagle
wget https://s3-eu-west-1.amazonaws.com/tabmo-eng/Bench/weather-api-finagle_2.11-0.0.1-one-jar.jar
JVM_OPTS="-Xms3000m -Xmx50000m -XX:+HeapDumpOnOutOfMemoryError -XX:MaxPermSize=128m -XX:+UseParNewGC -XX:MaxNewSize=256m -XX:NewSize=256m -XX:SurvivorRatio=128 -XX:MaxTenuringThreshold=0 -XX:+UseTLAB -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled"

java $JVM_OPTS -cp weather-api-finagle_2.11-0.0.1-one-jar.jar:. OneJar
```

Configuration (sinon erreurs) :

```
  ClientBuilder()
    .failFast(false)
    .hostConnectionMaxWaiters(Int.MaxValue)
    .hostConnectionLimit(Int.MaxValue)
```



## WRK (tester)

**c4.xlarge (4 cores)**

```
sudo apt-get update
sudo apt-get install build-essential libssl-dev git -y
git clone https://github.com/wg/wrk.git
cd wrk
make

./wrk -c4 -t4 -d120s "http://ec2-52-18-90-210.eu-west-1.compute.amazonaws.com/weather.json"
```

*Benchmark "just Nginx":*

c1-t1:   7600
c4-t4:   20000
c8-t1:   32000
c8-t4:   33000
c8-t8:   32500
c16-t4:  46300
c16-t8:  46400
c16-t16: 46600
c32-t1:  48000
c32-t8:  48100
c32-t16: 48000
c32-t32: 48100
c64-t4:  47700
c64-t8:  48000
c64-t16: 48000
c64-t32: 48000


# Results

All benchs last 120s.

## Spray

```
c1-t1
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   708.47us    3.71ms 101.16ms   99.28%
    Req/Sec     2.29k   165.76     2.51k    96.58%
  Latency Distribution
     50%  426.00us
     75%  447.00us
     90%  472.00us
     99%    0.91ms
  273070 requests in 2.00m, 44.27MB read
Requests/sec:   2275.56
Transfer/sec:    377.78KB


c4-t4
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.66ms    9.31ms 176.83ms   97.75%
    Req/Sec     2.18k   267.26     2.32k    94.90%
  Latency Distribution
     50%  444.00us
     75%  471.00us
     90%  503.00us
     99%   50.97ms
  1037456 requests in 2.00m, 168.20MB read
Requests/sec:   8638.31
Transfer/sec:      1.40MB


c8-t8
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  8 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.39ms   15.06ms 226.97ms   95.67%
    Req/Sec     1.82k   345.56     2.20k    84.31%
  Latency Distribution
     50%  494.00us
     75%  541.00us
     90%    0.87ms
     99%   81.69ms
  1731825 requests in 2.00m, 280.77MB read
Requests/sec:  14429.86
Transfer/sec:      2.34MB


c16-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  8 threads and 16 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.31ms    8.22ms 112.70ms   90.91%
    Req/Sec     2.50k   622.85     3.54k    64.91%
  Latency Distribution
     50%  614.00us
     75%  771.00us
     90%    5.49ms
     99%   37.02ms
  2385588 requests in 2.00m, 386.76MB read
Requests/sec:  19873.19
Transfer/sec:      3.22MB


c32-t8
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  8 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     2.83ms   13.78ms 339.62ms   97.00%
    Req/Sec     4.78k     0.86k    5.39k    90.47%
  Latency Distribution
     50%  777.00us
     75%    0.88ms
     90%    1.07ms
     99%   69.05ms
  4558008 requests in 2.00m, 738.97MB read
Requests/sec:  37978.44
Transfer/sec:      6.16MB


c64-t8
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  8 threads and 64 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.60ms   42.87ms   1.00s    93.37%
    Req/Sec     5.76k     1.13k    7.34k    88.05%
  Latency Distribution
     50%    1.20ms
     75%    1.46ms
     90%    3.30ms
     99%  192.72ms
  5480636 requests in 2.00m, 0.87GB read
Requests/sec:  45667.17
Transfer/sec:      7.40MB

c128-t8
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  8 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    10.46ms   39.63ms   1.14s    94.71%
    Req/Sec     5.79k     0.93k    7.70k    91.10%
  Latency Distribution
     50%    2.47ms
     75%    2.72ms
     90%    4.42ms
     99%  182.35ms
  5517891 requests in 2.00m, 0.87GB read
  Non-2xx or 3xx responses: 2
Requests/sec:  45976.17
Transfer/sec:      7.45MB


c128-t16
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  16 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    10.39ms   39.63ms   1.06s    94.77%
    Req/Sec     2.90k   503.98     3.78k    90.24%
  Latency Distribution
     50%    2.47ms
     75%    2.71ms
     90%    4.37ms
     99%  182.53ms
  5522834 requests in 2.00m, 0.87GB read
Requests/sec:  46014.18
Transfer/sec:      7.46MB

c256-t32
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:8080/weather/Montpellier/
  32 threads and 256 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    10.88ms   32.91ms   1.01s    95.80%
    Req/Sec     1.45k   240.14     2.90k    91.19%
  Latency Distribution
     50%    5.12ms
     75%    5.37ms
     90%    6.16ms
     99%  167.78ms
  5519485 requests in 2.00m, 0.87GB read
Requests/sec:  45969.92
Transfer/sec:      7.45MB
```

## Play

```
c1-t1
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   424.06us  109.66us  10.03ms   98.54%
    Req/Sec     2.38k    74.25     2.62k    71.17%
  Latency Distribution
     50%  413.00us
     75%  441.00us
     90%  461.00us
     99%  588.00us
  283684 requests in 2.00m, 39.23MB read
Requests/sec:   2364.03
Transfer/sec:    334.75KB


c4-t4
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   678.22us    4.95ms  78.95ms   99.72%
    Req/Sec     2.25k    87.63     2.39k    94.33%
  Latency Distribution
     50%  435.00us
     75%  461.00us
     90%  491.00us
     99%    1.46ms
  1074643 requests in 2.00m, 148.60MB read
Requests/sec:   8947.97
Transfer/sec:      1.24MB


c8-t8
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  8 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   673.00us    3.71ms  85.43ms   99.75%
    Req/Sec     1.96k    91.91     2.37k    99.18%
  Latency Distribution
     50%  499.00us
     75%  535.00us
     90%  573.00us
     99%    1.63ms
  1877684 requests in 2.00m, 259.65MB read
Requests/sec:  15634.33
Transfer/sec:      2.16MB


c16-t8
Running 2m test @ http://ec2-52-18-121-149.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  8 threads and 16 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   770.10us    3.56ms  80.46ms   99.79%0.
    Req/Sec     3.21k   137.46     4.38k    99.20%
  Latency Distribution
     50%  608.00us
     75%  661.00us
     90%  722.00us
     99%    2.00ms
  3064232 requests in 2.00m, 423.73MB read
Requests/sec:  25514.53
Transfer/sec:      3.53MB


c32-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  8 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     0.87ms  471.40us  25.08ms   93.24%
    Req/Sec     4.82k   134.01     6.32k    70.75%
  Latency Distribution
     50%  756.00us
     75%    0.86ms
     90%    1.11ms
     99%    2.88ms
  4607084 requests in 2.00m, 637.08MB read
Requests/sec:  38360.29
Transfer/sec:      5.30MB


c64-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  8 threads and 64 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    31.13ms  123.80ms   1.20s    94.40%
    Req/Sec     6.04k   775.62     8.93k    68.47%
  Latency Distribution
     50%    1.06ms
     75%    1.40ms
     90%   34.55ms
     99%  780.90ms
  5770957 requests in 2.00m, 798.02MB read
Requests/sec:  48051.30
Transfer/sec:      6.64MB


c128-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  8 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    87.60ms  193.68ms   1.84s    90.08%
    Req/Sec     6.02k     1.36k   12.67k    68.08%
  Latency Distribution
     50%    1.31ms
     75%   82.21ms
     90%  277.49ms
     99%  921.08ms
  5751009 requests in 2.00m, 795.27MB read
  Socket errors: connect 0, read 0, write 0, timeout 30
Requests/sec:  47885.28
Transfer/sec:      6.62MB


c128-t16
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  16 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    89.04ms  196.59ms   1.84s    89.96%
    Req/Sec     3.01k     0.98k    7.26k    67.38%
  Latency Distribution
     50%    1.30ms
     75%   84.04ms
     90%  287.68ms
     99%  927.49ms
  5747471 requests in 2.00m, 794.78MB read
  Socket errors: connect 0, read 0, write 0, timeout 33
Requests/sec:  47888.19
Transfer/sec:      6.62MB


c256-t32
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:9000/weather/Montpellier
  32 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    88.12ms  193.84ms   1.84s    89.97%
    Req/Sec     1.53k   692.11     3.88k    64.44%
  Latency Distribution
     50%    1.30ms
     75%   83.66ms
     90%  283.18ms
     99%  920.85ms
  5748978 requests in 2.00m, 794.98MB read
  Socket errors: connect 0, read 0, write 0, timeout 24
Requests/sec:  47885.84
Transfer/sec:      6.62MB
```

## Finagle

```
t1-c1
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   413.66us  112.56us   4.00ms   96.07%
    Req/Sec     2.44k   119.90     2.77k    70.33%
  Latency Distribution
     50%  398.00us
     75%  427.00us
     90%  463.00us
     99%  611.00us
  291006 requests in 2.00m, 29.70MB read
Requests/sec:   2425.04
Transfer/sec:    253.40KB


c4-t4
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   440.86us  388.04us  31.66ms   98.99%
    Req/Sec     2.39k    70.15     2.55k    71.84%
  Latency Distribution
     50%  410.00us
     75%  436.00us
     90%  467.00us
     99%    0.86ms
  1140254 requests in 2.00m, 116.36MB read
Requests/sec:   9494.22
Transfer/sec:      0.97MB


c8-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  8 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   511.07us  319.19us  10.39ms   98.18%
    Req/Sec     2.08k    45.84     2.33k    61.65%
  Latency Distribution
     50%  468.00us
     75%  506.00us
     90%  548.00us
     99%    2.39ms
  1986520 requests in 2.00m, 202.71MB read
Requests/sec:  16540.54
Transfer/sec:      1.69MB


c16-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  8 threads and 16 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   660.35us  440.94us  10.55ms   96.98%
    Req/Sec     3.28k    57.53     3.51k    73.30%
  Latency Distribution
     50%  582.00us
     75%  638.00us
     90%  716.00us
     99%    3.52ms
  3137482 requests in 2.00m, 320.16MB read
Requests/sec:  26143.32
Transfer/sec:      2.67MB


c32-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  8 threads and 32 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     0.94ms  693.81us  29.39ms   93.63%
    Req/Sec     4.74k   119.65     5.06k    68.91%
  Latency Distribution
     50%  756.00us
     75%    0.87ms
     90%    1.19ms
     99%    4.71ms
  4531447 requests in 2.00m, 462.40MB read
Requests/sec:  37759.66
Transfer/sec:      3.85MB


c64-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  8 threads and 64 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.14ms   68.63ms   1.20s    97.01%
    Req/Sec     6.03k   526.65     7.52k    70.53%
  Latency Distribution
     50%    1.03ms
     75%    1.50ms
     90%    4.61ms
     99%  269.61ms
  5764186 requests in 2.00m, 588.20MB read
Requests/sec:  48030.36
Transfer/sec:      4.90MB


c128-t8
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  8 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    72.50ms  168.85ms   1.84s    91.21%
    Req/Sec     5.95k     1.34k   10.42k    70.31%
  Latency Distribution
     50%    1.33ms
     75%   56.25ms
     90%  201.05ms
     99%  867.76ms
  5649606 requests in 2.00m, 576.50MB read
Requests/sec:  47074.95
Transfer/sec:      4.80MB


c128-t16
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  16 threads and 128 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    73.51ms  169.48ms   1.64s    91.12%
    Req/Sec     3.01k     0.91k    6.81k    68.50%
  Latency Distribution
     50%    1.34ms
     75%   60.61ms
     90%  202.20ms
     99%  869.64ms
  5744304 requests in 2.00m, 586.17MB read
Requests/sec:  47860.30
Transfer/sec:      4.88MB


c256-t32
Running 2m test @ http://ec2-52-16-40-151.eu-west-1.compute.amazonaws.com:8000/weather/Montpellier
  16 threads and 256 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   115.43ms  220.39ms   1.84s    88.03%
    Req/Sec     2.98k     1.02k   11.48k    68.98%
  Latency Distribution
     50%    1.66ms
     75%  140.00ms
     90%  390.70ms
     99%  970.17ms
  5696790 requests in 2.00m, 581.32MB read
  Socket errors: connect 0, read 0, write 0, timeout 30
Requests/sec:  47461.98
Transfer/sec:      4.84MB


```
