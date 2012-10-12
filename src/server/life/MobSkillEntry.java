package server.life;

public final class MobSkillEntry {
	public final int skillId;
	public final int level;
	
	public MobSkillEntry(int skillId, int level) {
		this.skillId = skillId;
		this.level = level;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + skillId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MobSkillEntry other = (MobSkillEntry) obj;
		if (level != other.level)
			return false;
		if (skillId != other.skillId)
			return false;
		return true;
	}

}
