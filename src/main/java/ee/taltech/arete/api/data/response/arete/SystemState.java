package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.sun.management.OperatingSystemMXBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.File;
import java.lang.management.ManagementFactory;


@Data
@Builder
@AllArgsConstructor
@JsonClassDescription("Current state of the machine")
public class SystemState {

    @JsonPropertyDescription("JVM CPU usage")
    private Double processCpuLoad;

    @JsonPropertyDescription("System CPU usage")
    private Double systemCpuLoad;

    @JsonPropertyDescription("System RAM total")
    private Long systemRAMTotal;

    @JsonPropertyDescription("System RAM usage")
    private Long systemRAMUsed;

    @JsonPropertyDescription("System total Disk space")
    private Long systemDiskSpaceTotal;

    @JsonPropertyDescription("System used Disk space")
    private Long systemDiskSpaceUsed;


    public SystemState() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        processCpuLoad = osBean.getProcessCpuLoad();
        systemCpuLoad = osBean.getSystemCpuLoad();
        systemRAMUsed = osBean.getFreePhysicalMemorySize();
        systemRAMTotal = osBean.getTotalPhysicalMemorySize();
        File root = new File("/");
        systemDiskSpaceTotal = root.getTotalSpace();
        systemDiskSpaceUsed = systemDiskSpaceTotal - root.getFreeSpace();
    }
}
