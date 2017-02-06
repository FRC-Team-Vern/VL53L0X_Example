import os
import pandas as pd
import re
import datetime
import matplotlib.pyplot as plt


if __name__ == '__main__':
    pd.options.mode.chained_assignment = None  # default='warn'
    in_csv_file = os.getcwd() + '20170205-190622.log'
    headers = ["Date Time", "Logger", "Address Title", "Address", "I/O Type", "Value"]
    log_df = pd.read_csv(filepath_or_buffer=in_csv_file, sep=',', header=None, names=headers, skiprows=2)
    log_df.fillna(0, inplace=True)

    # Modify Date and Time to Epoch milliseconds
    date_time = log_df["Date Time"]
    epoch = datetime.datetime.utcfromtimestamp(0)
    mapped_datetime = date_time.map(lambda x: datetime.datetime.strptime(x, '%Y/%m/%d %H:%M:%S.%f'))
    mapped_time = mapped_datetime.map(lambda dt: (dt - epoch).total_seconds() * 1000.0)
    min_val = mapped_time.min()
    mapped_min_time = mapped_time.map(lambda t: int(round((t - min_val), 0)))

    log_df["Time"] = mapped_min_time

    address1_df = log_df[log_df.Address == 1]
    address2_df = log_df[log_df.Address == 2]

    # Remove white space
    address1_df["I/O Type"] = address1_df["I/O Type"].map(lambda x: x.replace(" ", ""))
    address2_df["I/O Type"] = address2_df["I/O Type"].map(lambda x: x.replace(" ", ""))

    address1_power = address1_df[address1_df["I/O Type"] == "Power"]
    address1_input = address1_df[address1_df["I/O Type"] == "Input"]

    address2_power = address2_df[address2_df["I/O Type"] == "Power"]
    address2_input = address2_df[address2_df["I/O Type"] == "Input"]

    plt.figure(1)
    plt.subplot(411)
    # plt.xlim(0, 4000)
    plt.title('PID Range Finder')
    plt.ylabel('Address 1 Input (mm)')
    plt.xlabel('Time (ms)')
    plt.grid(True)
    plt.plot(address1_input.Time, address1_input.Value)
    plt.subplot(412)
    # plt.xlim(0, 4000)
    plt.ylabel('Address 1 Power (%)')
    plt.xlabel('Time (ms)')
    plt.grid(True)
    plt.plot(address1_power.Time, address1_power.Value)
    plt.subplot(413)
    # plt.xlim(0, 4000)
    plt.ylabel('Address 2 Input (mm)')
    plt.xlabel('Time (ms)')
    plt.grid(True)
    plt.plot(address2_input.Time, address2_input.Value)
    plt.subplot(414)
    # plt.xlim(0, 4000)
    plt.ylabel('Address 2 Power (%)')
    plt.xlabel('Time (ms)')
    plt.grid(True)
    plt.plot(address2_power.Time, address2_power.Value)

    plt.savefig("PIDRangeFinder_1.png")

    plt.show()
