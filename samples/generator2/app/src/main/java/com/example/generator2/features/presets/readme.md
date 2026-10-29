### База данных

| Column 1             | default     |   | Описание                                 |
|:---------------------|:------------|---|------------------------------------------|
| presetsName          | default     | S | Имя прессета                             |
| ch1_EN               | false       | S |                                          |   
| ch1_Carrier_Filename | Sine        | S |                                          |
| ch1_Carrier_Fr       | 400.0       | S |                                          |
| ch1_AM_EN            | false       | S |                                          |
| ch1_AM_Filename      | 01_SINE_12b | S |                                          |
| ch1_AM_Fr            | 8.7f        | S |                                          |
| ch1_FM_EN            | false       | S |                                          |
| ch1_FM_Filename      | 01_SINE_12b | S |                                          |
| ch1_FM_Dev           | 1100f       | S |                                          |
| ch1_FM_Fr            | 5.1f        | S |                                          |
| ch2_EN               | false       | S |                                          |   
| ch2_Carrier_Filename | Sine        | S |                                          |
| ch2_Carrier_Fr       | 400.0       | S |                                          |
| ch2_AM_EN            | false       | S |                                          |
| ch2_AM_Filename      | 01_SINE_12b | S |                                          |
| ch2_AM_Fr            | 8.7f        | S |                                          |
| ch2_FM_EN            | false       | S |                                          |
| ch2_FM_Filename      | 01_SINE_12b | S |                                          |
| ch2_FM_Dev           | 1100f       | S |                                          |
| ch2_FM_Fr            | 5.1f        | S |                                          |
| mono                 | false       | S |                                          |
| invert               | false       | S |                                          |
| shuffle              | false       | S |                                          |
| enL                  | true        | S |                                          |
| enR                  | true        | S |                                          |
| maxVolume0           | 0.9f        | S |                                          |
| maxVolume1           | 0.9f        | S |                                          |
| currentVolume0       | 1f          | S |                                          |
| currentVolume1       | 1f          | S |                                          |
| volume0              | 1f          | S |                                          |
| volume1              | 1f          | S |                                          |
| ch1AmDepth           | 1f          | S |                                          |
| ch2AmDepth           | 1f          | S |                                          |
| ch1FmMin             | 1000.0F     | S |                                          |
| ch1FmMax             | 2000.0F     | S |                                          |
| ch2FmMin             | 1000.0F     | S |                                          |
| ch2FmMax             | 2000.0F     | S |                                          |
| parameterInt0        | 0           | S | Режим выбора частот 0-обычный, 1-min max |
| parameterInt1        | 0           | S | Режим выбора частот 0-обычный, 1-min max |
| star                 |             | S |                                          |
| tp_EN                | false       | S | Режим «3-фаза» включён                    |
| tp_Pattern           | Circle      | S | Имя паттерна 3-фазы                      |
| tp_Velocity          | 1.0f        | S | Скорость паттерна 0.1..10                |
| tp_Amplitude         | 1.0f        | S | Размер траектории 0..1                   |
| tp_Carrier_Fr        | 700.0f      | S | Частота несущей 3-фазы, Гц               |
| tp_Volume            | 0.1f        | S | Громкость 3-фазы 0..1                    |
| tp_Calib_A           | 0.0f        | S | Калибровка электрода N, дБ               |
| tp_Calib_B           | 0.0f        | S | Калибровка электрода L, дБ               |
| tp_Calib_C           | 0.0f        | S | Калибровка электрода R, дБ               |
| tp_Center            | 13.0f       | S | Приглушение центра, % 0..20              |
|                      |             |   |                                          |

