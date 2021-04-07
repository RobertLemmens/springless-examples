# Simple crud server
This application does crud on a hashmap.

|         | Spring web                                                        | Vanilla                                                     |
|---------|-------------------------------------------------------------------|-------------------------------------------------------------|
| Note    | Straight forward, Controller code is easy and limited to just about 50 lines. | More code, some extra plumbing for json and writing responses is needed. We end up with around 140 lines. |
| Size    | 16mb                                | 7,14kb                        |
| Startup | ~ 1.5 - 2 seconds (1500 - 2000 ms)                                            | ~250 - 280 ms                                               |
