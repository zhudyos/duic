package io.zhudy.duic.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class SpringCloudResponseDto(
        val name: String,
        val profiles: List<String>,
        @JsonProperty("propertySources")
        val propertySources: List<SpringCloudPropertySource>
)

data class SpringCloudPropertySource(val name: String, val source: Any)