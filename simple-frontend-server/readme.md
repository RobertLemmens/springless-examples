# Simple frontend server
This application serves files from the resources directory. This can be simple http, css or javascript. Nothing more.

|         | Spring web                                                        | Vanilla                                                     |
|---------|-------------------------------------------------------------------|-------------------------------------------------------------|
| Note    | Straight forward, almost no code. But a dependency on spring web. | Some code. A basic impl with a try catch is about 50 lines. |
| Size    | 16mb (~250 bytes static resources)                                | 4,57kb (~250 bytes static resources)                        |
| Startup | ~ 1 - 1.5 seconds (1000 - 1500 ms)                                           | ~250 - 270 ms                                               |


So we see that we start about 5/6 times faster and are 3400 times smaller in size. This is because spring brings allot of stuff we dont actually need when serving just a static file, but we get it anyway.